Math.getRandomInt = function (min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
};


Object.size = function(obj) {
    var size = 0;
    if(obj!=undefined && obj!=null){
	    var key=null;
	    for (key in obj) {
	        if (obj.hasOwnProperty(key)) size++;
	    }
    }
    return size;
};


//compute intersections between array of number or strings, verify with 
// arrayIntersection( [1, 2, 3, "a"], [1, "a", 2], ["a", 1] ); -> [1,'a']

var arrayContains = Array.prototype.indexOf ?
	    function(arr, val) {
	        return arr.indexOf(val) > -1;
	    } :
	    function(arr, val) {
	        var i = arr.length;
	        while (i--) {
	            if (arr[i] === val) {
	                return true;
	            }
	        }
	        return false;
	    };

function arrayIntersection() {
	    var val, arrayCount, firstArr, i, j, intersection = [], missing;
	    var arrays = Array.prototype.slice.call(arguments); // Convert arguments into a real array

	    // Search for common values
	    firstArr = arrays.pop();
	    if (firstArr) {
	        j = firstArr.length;
	        arrayCount = arrays.length;
	        while (j--) {
	            val = firstArr[j];
	            missing = false;

	            // Check val is present in each remaining array 
	            i = arrayCount;
	            while (!missing && i--) {
	                if ( !arrayContains(arrays[i], val) ) {
	                    missing = true;
	                }
	            }
	            if (!missing) {
	                intersection.push(val);
	            }
	        }
	    }
	    return intersection;
	}