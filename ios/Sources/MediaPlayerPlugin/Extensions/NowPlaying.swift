import MediaPlayer

extension MediaPlayerView {

    func setNowPlayingImage() {
        if let artwork = self.extra?.poster {
            let session = URLSession(configuration: .default)
            let task = session.dataTask(with: artwork) { (data, response, error) in
                guard let imageData = data, error == nil else {
                    print("Error while downloading the image: \(error?.localizedDescription ?? "")")
                    return
                }

                let image = UIImage(data: imageData)
                DispatchQueue.main.async {
                    var nowPlayingInfo = MPNowPlayingInfoCenter.default().nowPlayingInfo ?? [String: Any]()
                    nowPlayingInfo[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(boundsSize: image?.size ?? CGSize.zero, requestHandler: { _ in
                        return image ?? UIImage()
                    })
                    MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
                }
            }
            task.resume()
        }
    }
    func setNowPlayingInfo() {
        var nowPlayingInfo = [String: Any]()

        if let title = self.extra?.title {
            nowPlayingInfo[MPMediaItemPropertyTitle] = title
        }
        if let subtitle = self.extra?.subtitle {
            nowPlayingInfo[MPMediaItemPropertyArtist] = subtitle
        }

        nowPlayingInfo[MPNowPlayingInfoPropertyMediaType] = NSNumber(value: MPNowPlayingInfoMediaType.video.rawValue)
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        
        UIApplication.shared.beginReceivingRemoteControlEvents()

        periodicTimeObserver = player?.addPeriodicTimeObserver(forInterval: CMTime(seconds: 0.1, preferredTimescale: CMTimeScale(NSEC_PER_SEC)), queue: nil) { time in
            var nowPlayingInfo = MPNowPlayingInfoCenter.default().nowPlayingInfo ?? [String: Any]()
            if let currentItem = self.player?.currentItem,
               let currentTime = self.player?.currentTime(),
               currentItem.status == .readyToPlay {

                let elapsedTime = CMTimeGetSeconds(currentTime)
                if currentItem.isPlaybackLikelyToKeepUp {
                    nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = self.player?.rate
                } else {
                    nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = 0
                }

                NotificationCenter.default.post(
                    name: .mediaPlayerTimeUpdate,
                    object: nil,
                    userInfo: ["playerId": self.playerId, "currentTime": currentTime.seconds]
                )

                nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = Float(elapsedTime)
                nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = currentItem.duration.seconds

                MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
            }
        }
    }
    func clearNowPlaying(){
        if let token = self.periodicTimeObserver {
            self.videoPlayer.player?.removeTimeObserver(token)
            self.periodicTimeObserver = nil
        }
        MPNowPlayingInfoCenter.default().nowPlayingInfo = [:]
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
    }
}
