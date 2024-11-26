import AVFoundation
import AVKit
import UIKit

extension MediaPlayerView: AVPlayerViewControllerDelegate {
    
    public func playerViewControllerWillStartPictureInPicture(_ playerViewController: AVPlayerViewController) {
        if self.ios.automaticallyHideBackgroundForPip == true {
            self.isHidden = true
        }
    }

    public func playerViewControllerWillStopPictureInPicture(_ playerViewController: AVPlayerViewController) {
        if self.ios.automaticallyHideBackgroundForPip == true {
            self.isHidden = false
        }
    }
    
    public func playerViewControllerRestoreUserInterfaceForPictureInPictureStop(_ playerViewController: AVPlayerViewController) {
        self.isHidden = false
    }
    
    public func playerViewControllerDidStartPictureInPicture(_ playerViewController: AVPlayerViewController){
        self.isInPipMode = true
        NotificationCenter.default.post(name: .mediaPlayerPictureInPicture, object: nil, userInfo: ["playerId": self.playerId, "isInPictureInPicture": true])
    }
    
    public func playerViewControllerDidStopPictureInPicture(_ playerViewController: AVPlayerViewController){
        self.isInPipMode = false
        NotificationCenter.default.post(name: .mediaPlayerPictureInPicture, object: nil, userInfo: ["playerId": self.playerId, "isInPictureInPicture": false])
    }
    
    public func playerViewController(
        _ playerViewController: AVPlayerViewController,
        willBeginFullScreenPresentationWithAnimationCoordinator coordinator: any UIViewControllerTransitionCoordinator
    ){
        self.isFullscreen = true
        coordinator.animate(alongsideTransition: nil) {_ in
            self.videoPlayer.view.superview?.bringSubviewToFront(self.videoPlayer.view)
        }
        NotificationCenter.default.post(name: .mediaPlayerFullscreen, object: nil, userInfo: ["playerId": self.playerId, "isInFullScreen": true])
    }
    
    public func playerViewController(
        _ playerViewController: AVPlayerViewController,
        willEndFullScreenPresentationWithAnimationCoordinator coordinator: any UIViewControllerTransitionCoordinator
    ){
        self.isFullscreen = false
        let isPlaying = self.videoPlayer.player?.timeControlStatus == .playing
        coordinator.animate(alongsideTransition: nil) { _ in
            if isPlaying {
                self.videoPlayer.player?.play()
            }
            self.superview?.bringSubviewToFront(self)
        }
    }
    
    public func playerViewController(
        _ playerViewController: AVPlayerViewController,
        didEndFullScreenPresentationWithAnimationCoordinator coordinator: any UIViewControllerTransitionCoordinator
    ){
        NotificationCenter.default.post(name: .mediaPlayerFullscreen, object: nil, userInfo: ["playerId": self.playerId, "isInFullScreen": false])
    }
    
    public func videoPlayer(
        _ playerViewController: AVPlayerViewController,
        timeToSeekAfterUserNavigatedFrom oldTime: CMTime,
        to targetTime: CMTime
    ) -> CMTime {
        NotificationCenter.default.post(name: .mediaPlayerSeeked, object: nil, userInfo: ["playerId": self.playerId, "previousTime": oldTime, "newTime": targetTime])
        return targetTime
    }
    
}
