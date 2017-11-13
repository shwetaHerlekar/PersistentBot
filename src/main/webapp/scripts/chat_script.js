var response = ""
var speak = true;
var synth = window.speechSynthesis;

$(function(){

	var sentHead = "<div class='row' style='margin: 5px 0px'> <div class='col-sm-offset-4 col-sm-8 text-right'> <div class='sent text-left'>";
	var receivedHead = "<div class='row' style='margin: 5px 0px'> <div class='col-sm-8 text-left'> <div class='received text-left'>";
	var tail = "</div> </div> </div>";
	var sessionID = md5((Math.floor(Math.random() * 10000000000)).toString())
	
	console.log(sessionID)
	
	function send() {
		var data = {
			"query" : $("#message").val(),
			"sessionId" : sessionID
		};


		$(sentHead+data.query+tail).hide().appendTo('.chatdiv').show("puff", {times : 3}, 200);

		$(".chatdiv").animate({ scrollTop: $('.chatdiv').prop("scrollHeight")}, 1000);
		$("#message").val("");

		$.get("/ai",data,function(res){
			console.log(res)
			rotation()

			/*speech = res.speech;
			res = res.displayText;
			
			console.log("res"+res)
			console.log("speech"+speech)

			res = res.replace(new RegExp(":ob", 'g'),"<button type='button' class='btn btn-md btn-default dbutton'>")
			res = res.replace(new RegExp(":cb", 'g'),"</button>")*/
			response = res
			$(receivedHead+response.replace(/(\\n|\n)/g, "<br>")+tail).hide().appendTo('.chatdiv').show("puff", {times : 3}, 200);
			$(".chatdiv").animate({ scrollTop: $('.chatdiv').prop("scrollHeight")}, 1000);

			if(speak)
			{
				var msg = new SpeechSynthesisUtterance();
	    		var voices = window.speechSynthesis.getVoices();
			    msg.voiceURI = "native";
			    msg.text = speech.replace( new RegExp(":ob[ a-zA-Z]*:cb", 'g'), "").replace(/(\\n|\n)/g, "").replace(new RegExp("<a.*a>", 'g'), "here");
			    msg.lang = "en-IN";
			    synth.speak(msg);
			}

		});

	};


	$("#message").keypress(function(event) {
		if (event.which == 13) {
			event.preventDefault();

			if( $("#message").val() != "")
				send();
		}
	});

	$("#rec").click(function(event) {
		switchRecognition();
		event.preventDefault();
		$("#message").val("Listening...")
	});

	$("#send").click(function(event){
		
		if( $("#message").val() != "")
			send()
	})

	var recognition;

	function startRecognition() {
		recognition = new webkitSpeechRecognition();
		recognition.onstart = function(event) {
			updateRec();
		};
		recognition.onresult = function(event) {
			var text = "";
		    for (var i = event.resultIndex; i < event.results.length; ++i) {
		    	text += event.results[i][0].transcript;
		    }
		    console.log("text:");
		    console.log(text)

		    setInput(text);
			stopRecognition();
		};
		
		recognition.onend = function() {
			stopRecognition();
		};

		recognition.lang = "en-IN";
		recognition.start();
	}

	function stopRecognition() {
		if (recognition) {
			recognition.stop();
			recognition = null;
		}
		updateRec();
	}

	function switchRecognition() {
		if (recognition) {
			stopRecognition();
		} else {
			startRecognition();
		}
	}

	function setInput(text) {
		$("#message").val(text);
		//send()
	}

	function updateRec() {
		$("#rec").attr('class', recognition ? "fa fa-microphone micspan_on fa-lg" : "fa fa-microphone micspan fa-lg")

		//$("#message").val(recognition ? "Listening..." : "");
	}



	var degrees = 360
	function rotation(){

	/*	$("#logo").css({'transform' : 'rotate('+ degrees +'deg)', 'transition' : 'transform 1s'});
		if (degrees == 360)
			degrees = 0 
		else
			degrees = 360*/

	}

	$(".chatdiv").on("click", "button", function(){
		var val = $(this).text()

		if(val != "")
		{
			$("#message").val(val)
			send()
		}

		$(".dbutton").remove()
	})

	$("#speak").click(function(event){

		event.stopPropagation();
		
		$("#speak").toggleClass("fa-volume-off");
		$("#speak").toggleClass("fa-volume-up");

		speak = !speak;
		if(!speak){
			synth.cancel();
		}

	});

	$("#speak-box").click(function(){
		$("#speak").trigger( "click" );
	})
});