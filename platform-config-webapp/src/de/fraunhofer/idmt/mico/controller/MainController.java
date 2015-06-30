package de.fraunhofer.idmt.mico.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;


 
/**
 * @author Marcel Sieland
 *
 */
@Controller
public class MainController  {
	
	private File cmdPropFileLnx = new File("/usr/share/mico/platform-config.properties");
	private Properties cmdProps = null;
	private String currentConfig = null;
	
	String base = "/home/user/Downloads/extractors-public/configurations/mico-config-extractors.sh";
	String host = "mico-platform-default";
	String user = "mico-default";
	String pw = "mico-default";
	
	@RequestMapping(value={"/configs","/run"})
	public ModelAndView configs(HttpServletRequest req) {
		setInits(req);
 
		ModelAndView modelAndView = new ModelAndView("result");
		Set<Object> propertyNames = getCmdProps().keySet();
		modelAndView.addObject("commands", propertyNames);
		if (currentConfig != null){
			modelAndView.addObject("status", "Active configuration: " + currentConfig);
		}else{
			modelAndView.addObject("status", "No configuration currently active");
		}

		return modelAndView;
	}
	
	/**
	 * resets the platform, by stopping all known configurations and their extractors
	 * @return ModelAndView for config page
	 */
	@RequestMapping(value={"/stopAll"})
	public ModelAndView reset(HttpServletRequest req) {
		String stopAll = stopAll();
		if ("OK".equals(stopAll)){
			currentConfig =null;
			return configs(req);
		}else{
			ModelAndView mav = configs(req);
			mav.addObject("error",stopAll);
			return mav;
		}
	}

	
	@RequestMapping(value="/run", method= RequestMethod.POST)
	public ModelAndView run(@RequestParam("command") String cmdName,
			HttpServletRequest req) throws InterruptedException, IOException {
		
		ModelAndView modelAndView = new ModelAndView("result");
		StringBuilder status = new StringBuilder();
		
		if(cmdName.equals(currentConfig)){
			status.append("Configuration ["+currentConfig+"] already started");
		}else{
			if (currentConfig != null){
				stopConfig(currentConfig);
				status.append("Old configuration ["+currentConfig+"] stopped");
				currentConfig = null;
			}
			System.out.println("command: " + cmdName);
			ProcessBuilder pb = getProcessBuilder(cmdName,true);
			modelAndView.addAllObjects(runCommand(pb));
			if (!modelAndView.getModel().containsKey("error") || modelAndView.getModel().get("error") != null){
				// if there was no error, the current config should be the new one
				currentConfig = cmdName;
			}
		}

		Set<Object> propertyNames = getCmdProps().keySet();

		modelAndView.addObject("commands", propertyNames);
		modelAndView.addObject("status", status.toString());
		
		return modelAndView;
	}

	private void setInits(HttpServletRequest req) {
		ServletContext context = req.getServletContext();
		base = context.getInitParameter("conf.script");
		String cmdPropFilePath = context.getInitParameter("conf.props");
		if (cmdPropFilePath != null && cmdPropFilePath.length() > 0)
			cmdPropFileLnx = new File(cmdPropFilePath);
		else
			cmdPropFileLnx = new File("/usr/share/mico/platform-config.properties");

		host = context.getInitParameter("mico.host") != null ? context.getInitParameter("mico.host") : "mico-platform";
		user = context.getInitParameter("mico.user") != null ? context.getInitParameter("mico.user") : "mico";
		pw =   context.getInitParameter("mico.pass") != null ? context.getInitParameter("mico.pass") : "mico";
	}

	private Map<String, String> runCommand(ProcessBuilder pb) throws IOException,
			InterruptedException {
		String error = null;
		Map<String,String> map = new HashMap<String, String>(); 
		try {
			Process process = pb.start();
			IOThreadHandler outputHandler = new IOThreadHandler(process.getInputStream());
			IOThreadHandler errorHandler = new IOThreadHandler(process.getErrorStream());
			outputHandler.start();
			errorHandler.start();
			process.waitFor();
			int exitValue = process.exitValue();
			if (exitValue != 0 || errorHandler.getOutput().length() > 0) {
				String errMsg = errorHandler.getOutput().toString();
				error = "unable to start selected configuration: Error Code "
						+ exitValue + "\n" + HtmlUtils.htmlEscape(errMsg);
				System.out.println("Error [" + exitValue + "] - " + error);
			}

			String message = HtmlUtils.htmlEscape(outputHandler.getOutput()
					.toString());
			map.put("message", message);
		} catch (IOException e) {
			error = e.getMessage();
		}
		map.put("error", error);
		return map;
	}

	private boolean stopConfig(String cmdName) {
		ProcessBuilder pb = getProcessBuilder(cmdName,false);
		try {
			runCommand(pb);
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}


	/**
	 * create a process builder to start or stop a configuration
	 * @param cmd - path to configuration script
	 * @param start - start or stop the configuration
	 * @return
	 */
	private ProcessBuilder getProcessBuilder(String cmdName,boolean start) {
		String cmd = getCmdProps().getProperty(cmdName);
		String mode = start?" start":" stop";
		String fullCMD = "sudo " + base + " " + cmd + " " + host + " " + user
				+ " " + pw + mode;
		ProcessBuilder pb = new ProcessBuilder("bash", "-c", fullCMD);
		return pb;
	}

	private String stopAll(){
		Properties props = getCmdProps();
		Enumeration<Object> keys = props.keys();
		while ( keys.hasMoreElements()){
			Object key = keys.nextElement();
			if(key instanceof String){
				ProcessBuilder pb = getProcessBuilder((String)key,false);
				try {
					runCommand(pb);
				} catch (IOException | InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return e.getLocalizedMessage();
				}
			}
		}
		return "OK";
	}

	private Properties getCmdProps(){
		if (cmdProps == null){
			cmdProps = new Properties();
		}else{
			cmdProps.clear();
		}
		try {
			if (cmdPropFileLnx.exists() && cmdPropFileLnx.canRead()) {
				cmdProps.load(new FileInputStream(cmdPropFileLnx));
			}else{
				cmdProps.put("ls", "ls");
				cmdProps.put("pwd", "pwd");
				cmdProps.put("data\u0020from\u0020drive\u0020C:", "dir .");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cmdProps;
	}
	
	private static class IOThreadHandler extends Thread {
		private InputStream inputStream;
		private StringBuilder output = new StringBuilder();

		IOThreadHandler(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		public void run() {
			Scanner br = null;
			try {
				br = new Scanner(new InputStreamReader(inputStream,Charset.forName("UTF-8")));
				String line = null;
				while (br.hasNextLine()) {
					line = br.nextLine();
					output.append(line
							+ System.getProperty("line.separator"));
				}
			} finally {
				br.close();
			}
		}

		public StringBuilder getOutput() {
			return output;
		}
	}
}
