
function showGraph(){
	document.getElementById('graph').classList.remove('hidden');
	
	document.getElementById('selectedExtractors').classList.add('hidden');
}

function showSelectedExtractors(){
	document.getElementById('selectedExtractors').classList.remove('hidden');
	
	document.getElementById('graph').classList.add('hidden');
}