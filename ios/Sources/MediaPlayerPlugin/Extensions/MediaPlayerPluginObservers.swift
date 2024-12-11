import Foundation
import UIKit
import AVFoundation

extension MediaPlayerPlugin {

    @objc func removeNotificationCenterObservers() {
        NotificationCenter.default.removeObserver(mediaPlayerPlayObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerPauseObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerEndedObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerReadyObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerSeekObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerTimeUpdatedObserver!)
        
        NotificationCenter.default.removeObserver(mediaPlayerFullscreenObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerPictureInPictureObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerIsPlayingInBackgroundObserver!)
    }

    @objc func addNotificationCenterObservers() {
        mediaPlayerReadyObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerReady, object: nil, queue: nil,
                         using: mediaPlayerReady)
        
        mediaPlayerPlayObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerPlay, object: nil, queue: nil,
                         using: mediaPlayerPlay)
        
        mediaPlayerPauseObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerPause, object: nil, queue: nil,
                        using: mediaPlayerPause)
        
        mediaPlayerEndedObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerEnded, object: nil, queue: nil,
                        using: mediaPlayerEnded)
        
        mediaPlayerSeekObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerSeek, object: nil, queue: nil,
                         using: mediaPlayerSeek)
        
        mediaPlayerTimeUpdatedObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerTimeUpdated, object: nil, queue: nil,
                         using: mediaPlayerTimeUpdated)
        
        mediaPlayerFullscreenObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerFullscreen, object: nil, queue: nil,
                         using: mediaPlayerFullscreen)
        
        mediaPlayerPictureInPictureObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerPictureInPicture, object: nil, queue: nil,
                         using: mediaPlayerPictureInPicture)
        
        mediaPlayerIsPlayingInBackgroundObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerIsPlayingInBackground, object: nil, queue: nil,
                         using: mediaPlayerIsPlayingInBackground)
        
        
    }

}
