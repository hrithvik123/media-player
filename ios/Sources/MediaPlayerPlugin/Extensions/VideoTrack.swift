import AVKit

extension MediaPlayerView {
    func videoTrackEnable(playerItem: AVPlayerItem?, enable: Bool) {
        guard playerItem != nil else { return }
        let tracks = playerItem!.tracks
        for playerItemTrack in tracks {
            if let mediaType = playerItemTrack.assetTrack?.mediaType {
                if mediaType == AVMediaType.video {
                    playerItemTrack.isEnabled = enable
                }
            }
        }
    }
}
