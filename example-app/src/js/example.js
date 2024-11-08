import { MediaPlayer } from '@eduardoroth&#x2F;media-player';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    MediaPlayer.echo({ value: inputValue })
}
