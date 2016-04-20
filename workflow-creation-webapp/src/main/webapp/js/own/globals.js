// Total dimensions of the svg graph

var svgWidth = 1118;
var svgHeight = 500;

var regExp={
		int :       /^(\s*\d+\s*)$/,
		intArray:   /^(\s*\d+\s*)(,\s*\d+\s*)*$/,
		float:      /^(\s*[-+]?[0-9]+\.[0-9]+\s*|\s*[-+]?\d+\s*)$/,
		floatArray: /^(\s*[-+]?[0-9]+\.[0-9]+\s*|\s*[-+]?\d+\s*)(,\s*[-+]?[0-9]+\.[0-9]+\s*|\s*,\s*[-+]?\d+\s*)*$/,
};

var WORKFLOW_PREFIX='WORKFLOW_ID'