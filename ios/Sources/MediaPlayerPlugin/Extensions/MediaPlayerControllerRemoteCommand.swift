import MediaPlayer

extension MediaPlayerController {
    func setRemoteCommandCenter() {
        let rcc = MPRemoteCommandCenter.shared()
        
        rcc.togglePlayPauseCommand.isEnabled = true
        rcc.togglePlayPauseCommand.addTarget {event in
            self.player.timeControlStatus == .playing ? self.player.pause() : self.player.play()
            return .success
        }
        
        rcc.playCommand.isEnabled = true
        rcc.playCommand.addTarget {event in
            self.player.play()
            return .success
        }
        rcc.pauseCommand.isEnabled = true
        rcc.pauseCommand.addTarget {event in
            self.player.pause()
            return .success
        }
        rcc.changePlaybackPositionCommand.isEnabled = true
        rcc.changePlaybackPositionCommand.addTarget {event in
            let seconds = (event as? MPChangePlaybackPositionCommandEvent)?.positionTime ?? 0
            let time = CMTime(seconds: seconds, preferredTimescale: 1)
            self.player.seek(to: time)
            return .success
        }
        rcc.skipForwardCommand.isEnabled = true
        rcc.skipForwardCommand.addTarget {event in
            if let currentItem = self.player.currentItem {
                let currentTime = CMTimeGetSeconds(currentItem.currentTime()) + 10
                self.player.seek(to: CMTimeMakeWithSeconds(currentTime, preferredTimescale: 1))
                return .success
            } else {
                return .commandFailed
            }
        }
        rcc.skipBackwardCommand.isEnabled = true
        rcc.skipBackwardCommand.addTarget {event in
            if let currentItem = self.player.currentItem {
                let currentTime = CMTimeGetSeconds(currentItem.currentTime()) - 10
                self.player.seek(to: CMTimeMakeWithSeconds(currentTime, preferredTimescale: 1))
                return .success
            } else {
                return .commandFailed
            }
        }
        
        // Next and previous track buttons are disabled because we don't have more than 1 video
        rcc.nextTrackCommand.isEnabled = false
        rcc.previousTrackCommand.isEnabled = false
    }
    
    func clearRemoteCommandCenter() {
        let rcc = MPRemoteCommandCenter.shared()
        rcc.changePlaybackPositionCommand.isEnabled = false
        rcc.playCommand.isEnabled = false
        rcc.pauseCommand.isEnabled = false
        rcc.skipForwardCommand.isEnabled = false
        rcc.skipBackwardCommand.isEnabled = false
    }
}
