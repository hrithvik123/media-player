import AVFoundation
import AVKit
import UIKit

extension MediaPlayerView: AVPlayerViewControllerDelegate {
    
    public func playerViewControllerWillStartPictureInPicture(_ playerViewController: AVPlayerViewController) {
        
    }

    public func playerViewControllerWillStopPictureInPicture(_ playerViewController: AVPlayerViewController) {
        
    }

    public func playerViewController(_ playerViewController: AVPlayerViewController, failedToStartPictureInPictureWithError error: Error) {
        
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
        NotificationCenter.default.post(name: .mediaPlayerFullscreen, object: nil, userInfo: ["playerId": self.playerId, "isInFullScreen": true])
    }
    
    public func playerViewController(
        _ playerViewController: AVPlayerViewController,
        willEndFullScreenPresentationWithAnimationCoordinator coordinator: any UIViewControllerTransitionCoordinator
    ){
        let videoFrame = CGRect(
            x: self.ios!.left,
            y: self.ios!.top,
            width: self.ios!.width,
            height: self.ios!.height
        )
        self.videoPlayer.view.bounds = videoFrame
        self.videoPlayer.view.frame = videoFrame
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
