// XML writer with attributes and smart attribute quote escaping 
var XmlElement=function(name,content,attributes){
    var att_str = ''
    if (attributes) { // tests false if this arg is missing!
        att_str = formatXmlAttributes(attributes)
    }
    var xml
    if (!content){
        xml='<' + name + att_str + '/>'
    }
    else {
        xml='<' + name + att_str + '>' + content + '</'+name+'>'
    }
    return xml
}

   
/*
   Format a dictionary of attributes into a string suitable
   for inserting into the start tag of an element.  Be smart
   about escaping embedded quotes in the attribute values.
*/
	formatXmlAttributes = function (attributes) {
		
	var APOS = "'"; QUOTE = '"'; AMP = '&';
	var ESCAPED_QUOTE = {  }
		ESCAPED_QUOTE[QUOTE] = '&quot;'
		ESCAPED_QUOTE[APOS] = '&apos;'
		ESCAPED_QUOTE[AMP] = '&amp;'
		
    var att_value
    var apos_pos, quot_pos
    var use_quote, escape, quote_to_escape
    var att_str
    var re
    var result = ''
   
    for (var att in attributes) {
        att_value = attributes[att]
        
        // Find first quote marks if any
        apos_pos = att_value.indexOf(APOS)
        quot_pos = att_value.indexOf(QUOTE)
        eand_pos = att_value.indexOf(AMP)
       
        // Determine which quote type to use around 
        // the attribute value
        if (apos_pos == -1 && quot_pos == -1) {
            att_str = ' ' + att + "='" + att_value +  "'"
            result += att_str
            continue
        }
        
        // Prefer the single quote unless forced to use double
        if (quot_pos != -1 && quot_pos < apos_pos) {
            use_quote = APOS
        }
        else {
            use_quote = QUOTE
        }
   
        // Figure out which kind of quote to escape
        // Use nice dictionary instead of yucky if-else nests
        escape = ESCAPED_QUOTE[use_quote]
        
        // Escape only the right kind of quote
        re = new RegExp(use_quote,'g')
        att_str = ' ' + att + '=' + use_quote + 
            att_value.replace(re, escape) + use_quote
        result += att_str
    }
    return result
}

	
	
function XmlPrettyPrint(xml) {
    var formatted = '';
    var reg = /(>)(<)(\/*)/g;
    xml = xml.replace(reg, '$1\r\n$2$3');
    var pad = 0;
    jQuery.each(xml.split('\r\n'), function(index, node) {
        var indent = 0;
        if (node.match( /.+<\/\w[^>]*>$/ )) {
            indent = 0;
        } else if (node.match( /^<\/\w/ )) {
            if (pad != 0) {
                pad -= 1;
            }
        } else if (node.match( /^<\w[^>]*[^\/]>.*$/ )) {
            indent = 1;
        } else {
            indent = 0;
        }
	        var padding = '';
        for (var i = 0; i < pad; i++) {
            padding += '  ';
        }
	        formatted += padding + node + '\r\n';
        pad += indent;
    });

    return formatted;
}
	
function test() {   
    var atts = {att1:"a1", 
        att2:"This is in \"double quotes\" and this is " +
         "in 'single quotes'",
        att3:"This is in 'single quotes' and this is in " +
         "\"double quotes\""}
    
    // Basic XML example
    alert(element('elem','This is a test'))
   
    // Nested elements
    var xml = XmlElement('p', 'This is ' + 
    element('strong','Bold Text') + 'inline')
    alert(xml)
   
    // Attributes with all kinds of embedded quotes
    alert(element('elem','This is a test', atts))
   
    // Empty element version
    alert(element('elem','', atts))    
}   