var listOfPlayers = [];

function loadYouTubeVideo(players) {

    for (let i = 0; i < players.length; i++) {


       listOfPlayers.push({id: 'player' + i, vid: players[i]});
    }


    var tag = document.createElement('script');
    tag.src = "https://www.youtube.com/iframe_api";
    tag.id="you_tube_api_tag";
    var firstScriptTag = document.getElementById('videoLoadScript');
    firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

}

function onYouTubeIframeAPIReady() {


    function addVideo(playerId, videoId) {

        var player = new YT.Player(playerId, {

            videoId: videoId,
            events: {
                'onStateChange': onPlayerStateChange
            }
        });

        function onPlayerStateChange(event) {
            if (event.data == YT.PlayerState.ENDED) {

                player.cueVideoById(videoId, 0)
            }
        }

    }

    listOfPlayers.forEach(p => addVideo(p.id, p.vid));

}