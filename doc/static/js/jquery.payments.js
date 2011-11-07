var timeoutid='';
var timeoutfield='';
function timeout_trigger(){	
	if(timeoutfield!==''){
		if(jQuery("#"+timeoutfield).val()!=''){
			jQuery("#"+timeoutfield).next('span').css({"display":"none"});
		}else{
			jQuery("#"+timeoutfield).next('span').css({"display":"inline"});
		}
	}
}

(function($) {

$.fn.numeric = function(config, callback)
{
	if(typeof config === 'boolean')
	{
		config = { decimal: config};
	}
	config = config || {};
	if(typeof config.negative == "undefined") config.negative = true;
	if(typeof config.nospaces == "undefined") config.nospaces = true;
	var decimal = (config.decimal === false) ? "" : config.decimal || ".";
	var negative = (config.negative === true) ? true : false;
	// callback function
	var callback = typeof callback == "function" ? callback : function(){};

	return this.data("numeric.decimal", decimal).data("numeric.nospaces", config.nospaces).data("numeric.negative", negative).data("numeric.callback", callback).keypress($.fn.numeric.keypress).keyup($.fn.numeric.keyup).blur($.fn.numeric.blur);
};

$.fn.numeric.keypress = function(e)
{
	var decimal = $.data(this, "numeric.decimal");
	var negative = $.data(this, "numeric.negative");
	var nospaces = $.data(this, "numeric.nospaces");
	var key = e.charCode ? e.charCode : e.keyCode ? e.keyCode : 0;
	if(key == 13 && this.nodeName.toLowerCase() == "input")
	{
		return true;
	}
	else if(key == 13)
	{
		return false;
	}
	var allow = false;
	// allow Ctrl+A
	if((e.ctrlKey && key == 97 /* firefox */) || (e.ctrlKey && key == 65) /* opera */) return true;
	// allow Ctrl+X (cut)
	if((e.ctrlKey && key == 120 /* firefox */) || (e.ctrlKey && key == 88) /* opera */) return true;
	// allow Ctrl+C (copy)
	if((e.ctrlKey && key == 99 /* firefox */) || (e.ctrlKey && key == 67) /* opera */) return true;
	// allow Ctrl+Z (undo)
	if((e.ctrlKey && key == 122 /* firefox */) || (e.ctrlKey && key == 90) /* opera */) return true;
	// allow or deny Ctrl+V (paste), Shift+Ins
	if((e.ctrlKey && key == 118 /* firefox */) || (e.ctrlKey && key == 86) /* opera */
	|| (e.shiftKey && key == 45)) return true;
	// if a number was not pressed
		
	if((key < 48 || key > 57) &&  (nospaces && key==32))
	{

		/* '-' only allowed at start and if negative numbers allowed */
		if(this.value.indexOf("-") != 0 && negative && key == 45 && (this.value.length == 0 || ($.fn.getSelectionStart(this)) == 0)) return true;
		/* only one decimal separator allowed */
		if(decimal && key == decimal.charCodeAt(0) && this.value.indexOf(decimal) != -1)
		{
			allow = false;
		}
		// check for other keys that have special purposes
		if(
			key != 8 /* backspace */ &&
			key != 9 /* tab */ &&
			key != 13 /* enter */ &&
			key != 35 /* end */ &&
			key != 36 /* home */ &&
			key != 37 /* left */ &&
			key != 39 /* right */ &&
			key != 46 /* del */
		)
		{
			allow = false;
		}
		else
		{
			// for detecting special keys (listed above)
			// IE does not support 'charCode' and ignores them in keypress anyway
			if(typeof e.charCode != "undefined")
			{
				// special keys have 'keyCode' and 'which' the same (e.g. backspace)
				if(e.keyCode == e.which && e.which != 0)
				{
					allow = true;
					// . and delete share the same code, don't allow . (will be set to true later if it is the decimal point)
					if(e.which == 46){ allow = false;}
				}
				// or keyCode != 0 and 'charCode'/'which' = 0
				else if(e.keyCode != 0 && e.charCode == 0 && e.which == 0)
				{
					allow = true;
				}
			}
		}
		// if key pressed is the decimal and it is not already in the field
		if(decimal && key == decimal.charCodeAt(0))
		{
			if(this.value.indexOf(decimal) == -1)
			{
				allow = true;
			}
			else
			{
				allow = false;
			}
		}
	}
	else
	{
		allow = true;
	}
	return allow;
};

$.fn.numeric.keyup = function(e)
{
	var val = this.value;
	if(val.length > 0)
	{
		// get carat (cursor) position
		var carat = $.fn.getSelectionStart(this);
		// get decimal character and determine if negatives are allowed
		var decimal = $.data(this, "numeric.decimal");
		var negative = $.data(this, "numeric.negative");
		
		// prepend a 0 if necessary
		if(decimal != "")
		{
			// find decimal point
			var dot = val.indexOf(decimal);
			// if dot at start, add 0 before
			if(dot == 0)
			{
				this.value = "0" + val;
			}
			// if dot at position 1, check if there is a - symbol before it
			if(dot == 1 && val.charAt(0) == "-")
			{
				this.value = "-0" + val.substring(1);
			}
			val = this.value;
		}
		
		// if pasted in, only allow the following characters
		var validChars = [0,1,2,3,4,5,6,7,8,9,'-',decimal];
		// get length of the value (to loop through)
		var length = val.length;
		// loop backwards (to prevent going out of bounds)
		for(var i = length - 1; i >= 0; i--)
		{
			var ch = val.charAt(i);
			// remove '-' if it is in the wrong place
			if(i != 0 && ch == "-")
			{
				val = val.substring(0, i) + val.substring(i + 1);
			}
			// remove character if it is at the start, a '-' and negatives aren't allowed
			else if(i == 0 && !negative && ch == "-")
			{
				val = val.substring(1);
			}
			var validChar = false;
			// loop through validChars
			for(var j = 0; j < validChars.length; j++)
			{
				// if it is valid, break out the loop
				if(ch == validChars[j])
				{
					validChar = true;
					break;
				}
			}
			// if not a valid character, or a space, remove
			if(!validChar)
			{
				val = val.substring(0, i) + val.substring(i + 1);
			}
		}
		// remove extra decimal characters
		var firstDecimal = val.indexOf(decimal);
		if(firstDecimal > 0)
		{
			for(var i = length - 1; i > firstDecimal; i--)
			{
				var ch = val.charAt(i);
				// remove decimal character
				if(ch == decimal)
				{
					val = val.substring(0, i) + val.substring(i + 1);
				}
			}
		}
		// set the value and prevent the cursor moving to the end
		this.value = val;
		$.fn.setSelection(this, carat);
	}
};

$.fn.numeric.blur = function()
{
	var decimal = $.data(this, "numeric.decimal");
	var callback = $.data(this, "numeric.callback");
	var val = this.value;
	if(val != "")
	{
		var re = new RegExp("^\\d+$|\\d*" + decimal + "\\d+");
		if(!re.exec(val))
		{
			callback.apply(this);
		}
	}
};

$.fn.removeNumeric = function()
{
	return this.data("numeric.decimal", null).data("numeric.negative", null).data("numeric.callback", null).unbind("keypress", $.fn.numeric.keypress).unbind("blur", $.fn.numeric.blur);
};

// Based on code from http://javascript.nwbox.com/cursor_position/ (Diego Perini <dperini@nwbox.com>)
$.fn.getSelectionStart = function(o)
{
	if (o.createTextRange)
	{
		var r = document.selection.createRange().duplicate();
		r.moveEnd('character', o.value.length);
		if (r.text == '') return o.value.length;
		return o.value.lastIndexOf(r.text);
	} else return o.selectionStart;
};

// set the selection, o is the object (input), p is the position ([start, end] or just start)
$.fn.setSelection = function(o, p)
{
	// if p is number, start and end are the same
	if(typeof p == "number") p = [p, p];
	// only set if p is an array of length 2
	if(p && p.constructor == Array && p.length == 2)
	{
		if (o.createTextRange)
		{
			var r = o.createTextRange();
			r.collapse(true);
			r.moveStart('character', p[0]);
			r.moveEnd('character', p[1]);
			r.select();
		}
		else if(o.setSelectionRange)
		{
			o.focus();
			o.setSelectionRange(p[0], p[1]);
		}
	}
};

	$.fn.paymentForm = function(settings) {
		settings = jQuery.extend({
			// Configuration related to overlay
			creditCard:Array('visa','mastercard','amex','discover'),
			creditCardNames:Array('Visa','Mastercard','American Express','Discover'),
			cardOffer:Array('visa','mastercard','amex','discover'),
			fieldsID:Array('creditcard','cardyear','cardmonth','cardsecurecode','cardpostal','cardnameon','cardcity','cardstate','cardopt','cardstreet'),
			
			creditcardok:'',
			creditcarderror1:'Enter your full credit card number',
			creditcarderror2:'Invalid Card Number',			
			creditcardholder:'Credit Card Number',
			creditcardoffer:'We accept {cards} only',
			monthyearerror2:'This card is expired',
			monthyearerror1:'Please enter the card\'s expiration',
			monthyearerror:'Month and year not correct',

			securecodeerror1:'Secure code is required',
			securecodeerror2:'Secure code not correct',
			monthholder:'mm',
			yearholder:'yy',
			securecodeholder:'Secure Code',

			cardnameonerror:'Enter your name as it appears on your card',
			cardnameonholder:'Name On Card',
			cardstreeterror:'Enter your credit card\'s billing street',			
			cardstreetholder:'Street Address',			
			cardoptholder:'Apt, Suite, Building (optional)',
			cityerror:'Enter your credit card\'s billing address',
			stateerror:'State is required',
			postalerror:'Postal is required',
			cityholder:'City',
			stateholder:'State',
			postalholder:'Postal Code',
			fieldsText:Array(
					'Enter the full credit card number',
					'Enter your card\'s expiration',
					'Enter your card\'s security code',
					'Enter your card billing postal code',
					'Enter your name as it appears on your card',
					'Enter your credit card billing city',
					'Enter your credit card billing state',
					'',
					'Enter your credit card\'s billing street'
					),
			debug:false,
		},settings);
		
		
		var jQueryMatchedObj = this; // This, in this context, refer to jQuery object
		
			
		function _initialize(){
			_initializehtml();			
			_initializeelements();
			_initializeaction();
			return true;
		}
		
		function _initializeelements(){
			$("#"+settings.fieldsID[0]).numeric({'nospaces':false});
			$("#"+settings.fieldsID[1]).numeric();
			$("#"+settings.fieldsID[2]).numeric();
			$("#"+settings.fieldsID[3]).numeric();
			
		}
		
		
		function _initializeaction(){
			
			$("#creditcardform").find('span').each(function(index) {
			    $(this).bind('click', function() {		    			 
					  $(this).prev('input').focus();
				});
			});
		
			$("#"+settings.fieldsID[5]).bind('blur', function() {
				validateCardname();				
			});

			$("#"+settings.fieldsID[2]).bind('blur', function() {
				if($(this).val().length==1){
					$(this).val('0'+$(this).val());
				}		
			});
			$("#"+settings.fieldsID[1]).bind('blur', function() {
				if($(this).val().length==1){
					$(this).val('0'+$(this).val());
				}
			});
			$("#"+settings.fieldsID[2]+", #"+settings.fieldsID[1]+", #"+settings.fieldsID[3]).bind('blur', function() {		
				validateMonthYearSecure();
			});
			
			$("#"+settings.fieldsID[9]).bind('blur', function() {
				validateAddress();				  	  	
			});
				
			
			$("#"+settings.fieldsID[6]+", #"+settings.fieldsID[7]+", #"+settings.fieldsID[4]).bind('blur', function() {		
					validateCSP();
				});				
		  	
		 	$("#creditcardform").find('input').each(function(index) {
			    $(this).bind('focus', function() {
			    	  timeoutid = setInterval('timeout_trigger()', 100);
			    	  timeoutfield=$(this).attr('id');
			    	  $(this).prev("div").find('p').each(function(index) {
							 $(this).fadeOut(0);
						});
			    	  if($(this).attr('id') == settings.fieldsID[1] || $(this).attr('id') == settings.fieldsID[2] || $(this).attr('id') == settings.fieldsID[3]){
			    	  		$("#errormonthyear").removeClass('error').html($("#"+$(this).attr('id')+"text").html()).fadeIn(0);
			    	  		
			    	  }else{
			    	  		if($(this).attr('id') == settings.fieldsID[6] || $(this).attr('id') == settings.fieldsID[7] || $(this).attr('id') == settings.fieldsID[4]){			    	  			
				    	  		$("#errorcity").html($("#"+$(this).attr('id')+"text").html()).fadeIn(0);
			    	  		}
			    	  		else{			    	  	  
					    	  $("#"+$(this).attr('id')+"text").fadeIn(0);	
					    	}
			    	  }
			    	  
			    	  
					  $(this).addClass('focused');
					  $(this).removeClass('focusederror');			 
					  					  
				});
				$(this).bind('blur', function() {			 
					 $("#"+$(this).attr('id')+"text").fadeOut(0);					 
					  clearInterval(timeoutid);					 
					  $(this).removeClass('focused');		 

				});
				$(this).bind('keyup', function() {					
					if($(this).val()!=''){
						$(this).next('span').css({"display":"none"});
					}else
					{
						$(this).next('span').css({"display":"inline"});
					}
				});        
			});
			$("#"+settings.fieldsID[0]).bind('blur', function() {
				$(this).val($(this).val().replace(/ /gi, ""));

		  		
		  		validateCC();
			});
			
			$("#creditcardform").bind('submit', function() {
				validateCC();
				validateMonthYearSecure();
				
				validateCardname();
				validateCSP();
				validateAddress();
				
				if(validateCC() && validateMonthYearSecure() && validateCardname() && validateCSP() && validateAddress()){
					if(settings.debug){
						alert(
							settings.fieldsID[0]+'='+$("#"+settings.fieldsID[0]).val()+'\n'+
							settings.fieldsID[2]+'='+$("#"+settings.fieldsID[2]).val()+'\n'+
							settings.fieldsID[1]+'='+$("#"+settings.fieldsID[1]).val()+'\n'+
							settings.fieldsID[3]+'='+$("#"+settings.fieldsID[3]).val()+'\n'+
							settings.fieldsID[5]+'='+$("#"+settings.fieldsID[5]).val()+'\n'+
							settings.fieldsID[9]+'='+$("#"+settings.fieldsID[9]).val()+'\n'+
							settings.fieldsID[8]+'='+$("#"+settings.fieldsID[8]).val()+'\n'+
							settings.fieldsID[6]+'='+$("#"+settings.fieldsID[6]).val()+'\n'+
							settings.fieldsID[7]+'='+$("#"+settings.fieldsID[7]).val()+'\n'+
							settings.fieldsID[4]+'='+$("#"+settings.fieldsID[4]).val()+'\n'
						);
						return false;
					}
					return true;
				}
				return false;
			});
			return true;
		}
		function _initializehtml() {
			
			$("#"+settings.fieldsID[0]).attr('tabindex','500');
			$("#"+settings.fieldsID[2]).attr('tabindex','501');
			$("#"+settings.fieldsID[1]).attr('tabindex','502');
			$("#"+settings.fieldsID[3]).attr('tabindex','503');
			$("#"+settings.fieldsID[5]).attr('tabindex','504');
			$("#"+settings.fieldsID[9]).attr('tabindex','505');
			$("#"+settings.fieldsID[8]).attr('tabindex','506');
			$("#"+settings.fieldsID[6]).attr('tabindex','507');
			$("#"+settings.fieldsID[7]).attr('tabindex','508');
			$("#"+settings.fieldsID[4]).attr('tabindex','509');
			
			$("#"+settings.fieldsID[0]).addClass("creditcard");
			var creditcardhtml='<div class="sidetip">';
				creditcardhtml+='<p class="ok isaok" id="creditcardok">'+settings.creditcardok+'</p>';
				creditcardhtml+='<p class="blank error" id="creditcarderror2">'+settings.creditcarderror2+'</p>';
				creditcardhtml+='<p class="blank " id="creditcarderror1">'+settings.creditcarderror1+'</p>';
				creditcardhtml+='<p class="blank " id="creditcardtext">'+settings.fieldsText[0]+'</p>';
				var creditcardoffer='';
				for(i in settings.creditCard){
					if(inArray(settings.creditCard[i],settings.cardOffer)){
						creditcardoffer+=settings.creditCardNames[i]+", ";		
					}
				}				
				creditcardoffer=creditcardoffer.substr(0,creditcardoffer.length-2);
				creditcardhtml+='<p class="blank error" id="creditcardoffer">'+settings.creditcardoffer.replace(/{cards}/gi, creditcardoffer)+'</p>';
			creditcardhtml+='</div>';	 
			$("#"+settings.fieldsID[0]).before(creditcardhtml);
			creditcardhtml='<div class="card" >';
			for(i in settings.creditCard){
				creditcardhtml+='<img style="display:none;" class="imgnotoffer" id="'+settings.creditCard[i]+'" src="images/'+settings.creditCard[i]+'_mid.png"/>';
			}
			
			creditcardhtml+='</div>';
			$("#"+settings.fieldsID[0]).after(creditcardhtml);
			for(i in settings.cardOffer){
				$("#"+settings.cardOffer[i]).removeClass('imgnotoffer');
				$("#"+settings.cardOffer[i]).fadeIn(0);										
			}			
			creditcardhtml='<span class="holder" style="z-index:6;">'+settings.creditcardholder+'</span>';
			$("#"+settings.fieldsID[0]).after(creditcardhtml);
			
			
			
			
			var monthhtml='<div class="sidetip">';
				monthhtml+='<p class="blank " id="cardyeartext">'+settings.fieldsText[1]+'</p>';
				monthhtml+='<p class="blank " id="cardmonthtext">'+settings.fieldsText[1]+'</p>';
				monthhtml+='<p class="blank " id="cardsecurecodetext">'+settings.fieldsText[2]+'</p>';
				monthhtml+='<p class="blank error" id="errormonthyear">'+settings.monthyearerror+'</p>';          
				monthhtml+='<p class="blank " id="errorsecurecode1">'+settings.securecodeerror1+'</p>';
				monthhtml+='<p class="blank error" id="errorsecurecode2">'+settings.securecodeerror2+'</p>';
				
			monthhtml+='</div>';
			$("#"+settings.fieldsID[2]).parent().html(monthhtml+$("#"+settings.fieldsID[2]).parent().html());
			monthhtml='<span class="holder" style="margin-left: 92px;">'+settings.monthholder+'</span>';
			$("#"+settings.fieldsID[2]).after(monthhtml);

			var yearhtml='<span class="holder" style="margin-left: 142px;">'+settings.yearholder+'</span>';
			$("#"+settings.fieldsID[1]).after(yearhtml);

			var securecodehtml='<span class="holder" style="margin-left: 194px;">'+settings.securecodeholder+'</span>';
			$("#"+settings.fieldsID[3]).after(securecodehtml);
			
			var cardnameonhtml='<div class="sidetip">';
			cardnameonhtml+='<p class="blank" id="cardnameonerror">'+settings.cardnameonerror+'</p>';          
			cardnameonhtml+='<p class="blank" id="cardnameontext">'+settings.fieldsText[4]+'</p>';          			
			
			cardnameonhtml+='</div>';	
			$("#"+settings.fieldsID[5]).before(cardnameonhtml);
			cardnameonhtml='<span class="holder">'+settings.cardnameonholder+'</span>';
			$("#"+settings.fieldsID[5]).after(cardnameonhtml);

			var cardstreethtml='<div class="sidetip">';
			cardstreethtml+='<p class="blank" id="cardstreeterror">'+settings.cardstreeterror+'</p>';
			cardstreethtml+='<p class="blank " id="cardstreettext">'+settings.fieldsText[8]+'</p>';          
			cardstreethtml+='</div>';	
			$("#"+settings.fieldsID[9]).before(cardstreethtml);
			cardstreethtml='<span class="holder">'+settings.cardstreetholder+'</span>';
			$("#"+settings.fieldsID[9]).after(cardstreethtml);


			cardopthtml='<span class="holder">'+settings.cardoptholder+'</span>';
			
			$("#"+settings.fieldsID[8]).after(cardopthtml);
			var cityhtml='<div class="sidetip">';
				cityhtml+='<p class="blank " id="errorcity">'+settings.cityerror+'</p>';
				cityhtml+='<p class="blank error" id="errorstate">'+settings.stateerror+'</p>';
				cityhtml+='<p class="blank error" id="errorpostal">'+settings.postalerror+'</p>';
				cityhtml+='<p class="blank " id="cardcitytext">'+settings.fieldsText[5]+'</p>'; 
				cityhtml+='<p class="blank " id="cardstatetext">'+settings.fieldsText[6]+'</p>'; 
				cityhtml+='<p class="blank " id="cardpostaltext">'+settings.fieldsText[3]+'</p>'; 
			cityhtml+='</div>';
			$("#"+settings.fieldsID[6]).before(cityhtml);
			cityhtml='<span class="holder">'+settings.cityholder+'</span>';
			$("#"+settings.fieldsID[6]).after(cityhtml);

			var statehtml='<span class="holder" style="margin-left: 248px;">'+settings.stateholder+'</span>';
			$("#"+settings.fieldsID[7]).after(statehtml);
			
			var postalhtml='<span class="holder" style="margin-left: 318px;">'+settings.postalholder+'</span>';
			$("#"+settings.fieldsID[4]).after(postalhtml);
			
			$("#creditcardform").addClass('withjs');
			
			return true;
		}
		
		function validateMonthYearSecure(){
			var el1=$("#"+settings.fieldsID[2]);
			var el2=$("#"+settings.fieldsID[1]);			
			
			if($(el1).val()=='' || $(el2).val()==''){				
	  	  		$("#errormonthyear").removeClass('error');
	  	  		$("#monthtext").fadeIn(0);
	  	  		$("#errormonthyear").html(settings.monthyearerror).css({"display":"block"});	  	  		
	  	  		return false;
	  	  	}else{	
	  	  		if($(el1).val()>12 || $(el1).val()<1){	  	  	
	  	  			$("#errormonthyear").addClass('error');
	  	  			$("#errormonthyear").html(settings.monthyearerror);
	  	  			$("#errormonthyear").css({"display":"block"});	  	  			
	  	  			return false;
	  	  		}else{
	  	  			var nowDate = new Date();
	  	  			var enterDate=new Date();
	  	  			enterDate.setFullYear('20'+$(el2).val().toString());	  	  			
	  	  			enterDate.setUTCMonth(($(el1).val()/1)-1);
	  	  			if(enterDate<nowDate){
		  	  			$("#errormonthyear").addClass('error');
		  	  			$("#errormonthyear").html(settings.monthyearerror2);
		  	  			$("#errormonthyear").css({"display":"block"});
		  	  			return false;
		  	  		}
		  	  		else{
			  	  		$("#errormonthyear").css({"display":"none"});
			  	  		var el=$("#"+settings.fieldsID[3]);
						
						if($(el).val()==''){				
				  	  		$("#errormonthyear").html(settings.securecodeerror1).css({"display":"block"});
				  	  		return false;
				  	  	}else{		  	  					  	  		
				  	  		if($(el).val().length<3){
				  	  			$("#errormonthyear").html(settings.securecodeerror2).css({"display":"block"});
				  	  			return false;
				  	  		}else{
				  	  			return true;
				  	  		}
				  	  	}
			  	  		return true;
		  	  		}
		  	  	}
	  	  		
	  	  	}
		}
		
		function validateCardname(){
			var el=$("#"+settings.fieldsID[5]);
			$(el).prev("div").find("p").each(function(index){
		  		$(this).css({"display":"none"});		  		
			});
			if($(el).val()==''){
	  	  		$("#cardnameonerror").fadeIn(0);
				return false;	  	  		
	  	  	}
	  	  	return true;
		}
		function validateAddress(){
			var el=$("#"+settings.fieldsID[9]);
			$(el).prev("div").find("p").each(function(index){
		  		$(this).css({"display":"none"});		  		
			});
			if($(el).val()==''){
	  	  		$("#cardstreeterror").fadeIn(0);
				return false;	  	  		
	  	  	}
	  	  	return true;
		}
		function validateCSP(){
			
			var el1=$("#"+settings.fieldsID[6]);
			var el2=$("#"+settings.fieldsID[7]);
			var el3=$("#"+settings.fieldsID[4]);						
			if($(el1).val()==''){
				$("#errorcity").html(settings.fieldsText[5]).fadeIn(0);	  	  		
	  	  		return false;	  	  		
	  	  	}
	  	  	if($(el2).val()==''){
				$("#errorcity").html(settings.fieldsText[6]).fadeIn(0);	  	  		
	  	  		return false;	  	  		
	  	  	}
	  	  	if($(el3).val()==''){
				$("#errorcity").html(settings.fieldsText[3]).fadeIn(0);	  	  		
	  	  		return false;	  	  		
	  	  	}
	  	  	else{		  	  	
	  	  		$("#errorcity").css({"display":"none"});
	  	  		return true;
	  	  	}
		}
		
		function validateCC(){
			var el=$("#"+settings.fieldsID[0]);
			$("#"+settings.creditcardoffer).fadeOut(0);
			for(i in settings.creditCard){
				if(!inArray(settings.creditCard[i],settings.cardOffer)){
					$("#"+settings.creditCard[i]).fadeOut();
				}
			}
			$(el).prev("div").find("p").each(function(index){
				$(this).css({"display":"none"});
			});
		  	if($(el).val()==''){
		  		$("#creditcardtext").css({"display":"block"});
		  		return false;
		  	}
		  	else{ 		  			  		 	  	  				
		  		for(i in settings.creditCard){
		  			$("#"+settings.creditCard[i]).removeClass('cardshow');
		  		}  			  			  			  			
		  		if(isCreditCard($(el).val()) && (isVisa($(el).val()) || isMasterCard($(el).val()) || isAmericanExpress($(el).val()) || isDiscover($(el).val()))){
		  			var flag=false;
		  			if(isVisa($(el).val())){
		  				if(!inArray('visa',settings.cardOffer)){		  					
		  					flag=true;
		  				}
		  				$("#"+settings.creditCard[0]).addClass('cardshow');
		  			}
		  			if(isMasterCard($(el).val())){
		  				if(!inArray('mastercard',settings.cardOffer)){		  					
		  					flag=true;
		  				}
		  				$("#"+settings.creditCard[1]).addClass('cardshow');
		  			}
		  			if(isAmericanExpress($(el).val())){
		  				if(!inArray('amex',settings.cardOffer)){		  					
		  					flag=true;
		  				}
		  				$("#"+settings.creditCard[2]).addClass('cardshow');
		  			}
		  			if(isDiscover($(el).val())){
		  				if(!inArray('discover',settings.cardOffer)){		  					
		  					flag=true;
		  				}
		  				$("#"+settings.creditCard[3]).addClass('cardshow');
		  			}		
		  			if(flag==true){
		  				for(i in settings.cardOffer){
		  					$("#"+settings.cardOffer[i]).addClass('cardshow');
		  				}		  	
		  				$("#"+settings.fieldsID[0]).addClass('focusederror');		
		  				$("#creditcardoffer").fadeIn(0);		  				
		  				return false;
		  			}
		  			$(el).prev("div").find(".ok.isaok").css({"display":"block"});
		  			return true;
		  		}else{
		  			$("#creditcarderror2").css({"display":"block"});  			
		  			return false;
		  		}
		  	}
			return false;
		}
		
		function inArray(needle, haystack) {
		    var length = haystack.length;
		    for(var i = 0; i < length; i++) {
		        if(typeof haystack[i] == 'object') {
		            if(arrayCompare(haystack[i], needle)) return true;
		        } else {
		            if(haystack[i] == needle) return true;
		        }
		    }
		    return false;
		}
		
		
		 function isCreditCard(st) 
		 {
		 	if (st.length > 19)
		 		return (false);
		 	sum = 0; mul = 1; l = st.length;
		 	for (i = 0; i < l; i++) 
		 	{
		 		digit = st.substring(l-i-1,l-i);
		 		tproduct = parseInt(digit ,10)*mul;
		 		if (tproduct >= 10)
		 			sum += (tproduct % 10) + 1;
		 		else
		 			sum += tproduct;
		 		if (mul == 1)
		 			mul++;
		 		else
		 			mul--;
		 	}
		 	if ((sum % 10) == 0)
		 		return (true);
		 	else
		 		return (false);
		 }


		 function isVisa(cc)
		 {
		 	if (((cc.length == 16) || (cc.length == 13)) &&	(cc.substring(0,1) == 4))
		 		return isCreditCard(cc);
		 	return false;
		 } 

		 function isMasterCard(cc)
		 {
		 	firstdig = cc.substring(0,1);
		 	seconddig = cc.substring(1,2);
		 	if ((cc.length == 16) && (firstdig == 5) && ((seconddig >= 1) && (seconddig <= 5)))
		 		return isCreditCard(cc);
		 	return false;
		 }

		 function isAmericanExpress(cc)
		 {
		 	firstdig = cc.substring(0,1);
		 	seconddig = cc.substring(1,2);
		 	if ((cc.length == 15) && (firstdig == 3) && ((seconddig == 4) || (seconddig == 7)))
		 		return isCreditCard(cc);
		 	return false;
		 }

		 function isDiscover(cc)
		 {
		 	first4digs = cc.substring(0,4);
		 	if ((cc.length == 16) && (first4digs == "6011"))
		 		return isCreditCard(cc);
		 	return false;
		 }
		
		return _initialize();
	};
})(jQuery);

$(document).ready(function() {
	$('#creditcardform').paymentForm();
	$("#creditcardform").find('input').each(function(index) {
 		if ($(this).val() != '') {
 			$(this).next('span').css({"display":"none"});
 		}
	});
	$("#creditcardform").find('input').change(function() {
		if ($(this).val() != '') {
 			$(this).next('span').css({"display":"none"});
 		} else {
 			$(this).next('span').css({"display":"inline"});
 		}
	});
});