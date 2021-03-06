#!/bin/sh
#
# Based on the startup script /etc/init.d/tomcat7
#
# Written by Miquel van Smoorenburg <miquels@cistron.nl>.
# Modified for Debian GNU/Linux	by Ian Murdock <imurdock@gnu.ai.mit.edu>.
# Modified for Tomcat by Stefan Gybas <sgybas@debian.org>.
# Modified for Tomcat6 by Thierry Carrez <thierry.carrez@ubuntu.com>.
# Modified for Tomcat7 by Ernesto Hernandez-Novich <emhn@itverx.com.ve>.
# Additional improvements by Jason Brittain <jason.brittain@mulesoft.com>.
# Modified for Docker by Horst Stadler <horst.stadler@salzburgresearch.at>.


set -e

PATH=/bin:/usr/bin:/sbin:/usr/sbin
NAME=tomcat7
DESC="Tomcat servlet engine"
DEFAULT=/etc/default/$NAME
JVM_TMP=/tmp/tomcat7-$NAME-tmp

if [ `id -u` -ne 0 ]; then
	echo "You need root privileges to run this script"
	exit 1
fi
 
# Make sure tomcat is started with system locale
if [ -r /etc/default/locale ]; then
	. /etc/default/locale
	export LANG
fi

. /lib/lsb/init-functions

if [ -r /etc/default/rcS ]; then
	. /etc/default/rcS
fi


# The following variables can be overwritten in $DEFAULT

# Run Tomcat 7 as this user ID and group ID
TOMCAT7_USER=tomcat7
TOMCAT7_GROUP=tomcat7

# this is a work-around until there is a suitable runtime replacement 
# for dpkg-architecture for arch:all packages
# this function sets the variable JDK_DIRS
find_jdks()
{
    for java_version in 9 8 7 6
    do
        for jvmdir in /usr/lib/jvm/java-${java_version}-openjdk-* \
                      /usr/lib/jvm/jdk-${java_version}-oracle-* \
                      /usr/lib/jvm/jre-${java_version}-oracle-*
        do
            if [ -d "${jvmdir}" -a "${jvmdir}" != "/usr/lib/jvm/java-${java_version}-openjdk-common" ]
            then
                JDK_DIRS="${JDK_DIRS} ${jvmdir}"
            fi
        done
    done

    # Add older non multi arch installations
    JDK_DIRS="${JDK_DIRS} /usr/lib/jvm/java-6-openjdk /usr/lib/jvm/java-6-sun /usr/lib/jvm/java-7-oracle"
}

# The first existing directory is used for JAVA_HOME (if JAVA_HOME is not
# defined in $DEFAULT)
JDK_DIRS="/usr/lib/jvm/default-java"
find_jdks

# Look for the right JVM to use
for jdir in $JDK_DIRS; do
    if [ -r "$jdir/bin/java" -a -z "${JAVA_HOME}" ]; then
	JAVA_HOME="$jdir"
    fi
done
export JAVA_HOME

# Directory where the Tomcat 6 binary distribution resides
CATALINA_HOME=/usr/share/$NAME

# Directory for per-instance configuration files and webapps
CATALINA_BASE=/var/lib/$NAME

# Use the Java security manager? (yes/no)
TOMCAT7_SECURITY=no

# Default Java options
# Set java.awt.headless=true if JAVA_OPTS is not set so the
# Xalan XSL transformer can work without X11 display on JDK 1.4+
# It also looks like the default heap size of 64M is not enough for most cases
# so the maximum heap size is set to 128M
if [ -z "$JAVA_OPTS" ]; then
	JAVA_OPTS="-Djava.awt.headless=true -Xmx128M"
fi

# End of variables that can be overwritten in $DEFAULT

# overwrite settings from default file
if [ -f "$DEFAULT" ]; then
	. "$DEFAULT"
fi

if [ ! -f "$CATALINA_HOME/bin/bootstrap.jar" ]; then
	log_failure_msg "$NAME is not installed"
	exit 1
fi

POLICY_CACHE="$CATALINA_BASE/work/catalina.policy"

if [ -z "$CATALINA_TMPDIR" ]; then
	CATALINA_TMPDIR="$JVM_TMP"
fi

# Set the JSP compiler if set in the tomcat7.default file
if [ -n "$JSP_COMPILER" ]; then
	JAVA_OPTS="$JAVA_OPTS -Dbuild.compiler=\"$JSP_COMPILER\""
fi

SECURITY=""
if [ "$TOMCAT7_SECURITY" = "yes" ]; then
	SECURITY="-security"
fi

# Define other required variables
CATALINA_PID="/var/run/$NAME.pid"
CATALINA_SH="$CATALINA_HOME/bin/catalina.sh"

# Look for Java Secure Sockets Extension (JSSE) JARs
if [ -z "${JSSE_HOME}" -a -r "${JAVA_HOME}/jre/lib/jsse.jar" ]; then
    JSSE_HOME="${JAVA_HOME}/jre/"
fi

catalina_sh() {
	# Escape any double quotes in the value of JAVA_OPTS
	JAVA_OPTS="$(echo $JAVA_OPTS | sed 's/\"/\\\"/g')"

	AUTHBIND_COMMAND=""
	if [ "$AUTHBIND" = "yes" -a "$1" = "start" ]; then
		AUTHBIND_COMMAND="/usr/bin/authbind --deep /bin/bash -c "
	fi

	# Define the command to run Tomcat's catalina.sh
	# set -a tells sh to export assigned variables to spawned shells.
	TOMCAT_SH="set -a; JAVA_HOME=\"$JAVA_HOME\"; source \"$DEFAULT\"; \
		CATALINA_HOME=\"$CATALINA_HOME\"; \
		CATALINA_BASE=\"$CATALINA_BASE\"; \
		JAVA_OPTS=\"$JAVA_OPTS\"; \
		CATALINA_PID=\"$CATALINA_PID\"; \
		CATALINA_TMPDIR=\"$CATALINA_TMPDIR\"; \
		LANG=\"$LANG\"; JSSE_HOME=\"$JSSE_HOME\"; \
		cd \"$CATALINA_BASE\"; \
		\"$CATALINA_SH\" $@"

	if [ "$AUTHBIND" = "yes" -a "$1" = "start" ]; then
		TOMCAT_SH="'$TOMCAT_SH'"
	fi

	# Run the catalina.sh script foreground
	set +e
	touch "$CATALINA_PID" "$CATALINA_BASE"/logs/catalina.out
	chown $TOMCAT7_USER "$CATALINA_PID" "$CATALINA_BASE"/logs/catalina.out
	sudo -u "$TOMCAT7_USER" -g "$TOMCAT7_GROUP" -s /bin/bash -c "echo \$\$ >$CATALINA_PID; cd $CATALINA_TMPDIR && $AUTHBIND_COMMAND $TOMCAT_SH"

	rm -f "$CATALINA_PID"
	set +a -e
}


if [ -z "$JAVA_HOME" ]; then
	log_failure_msg "no JDK or JRE found - please set JAVA_HOME"
	exit 1
fi

if [ ! -d "$CATALINA_BASE/conf" ]; then
	log_failure_msg "invalid CATALINA_BASE: $CATALINA_BASE"
	exit 1
fi

log_daemon_msg "Starting $DESC" "$NAME"
if [ -f "$CATALINA_PID" ]; then
	if ps -p $(cat "$CATALINA_PID") > /dev/null; then
        	log_failure_msg "(already running)"
		exit 1
	else
		rm -f "$CATALINA_PID"
	fi
fi


# Regenerate POLICY_CACHE file
umask 022
echo "// AUTO-GENERATED FILE from /etc/tomcat7/policy.d/" \
	> "$POLICY_CACHE"
echo ""  >> "$POLICY_CACHE"
cat $CATALINA_BASE/conf/policy.d/*.policy \
	>> "$POLICY_CACHE"

# Remove / recreate JVM_TMP directory
rm -rf "$JVM_TMP"
mkdir -p "$JVM_TMP" || {
	log_failure_msg "could not create JVM temporary directory"
	exit 1
}
chown $TOMCAT7_USER "$JVM_TMP"

catalina_sh run $SECURITY

exit 0
