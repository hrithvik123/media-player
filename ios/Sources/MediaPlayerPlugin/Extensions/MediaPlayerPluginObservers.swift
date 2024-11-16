import Foundation
import UIKit
import AVFoundation

extension MediaPlayerPlugin {

    @objc func removeNotificationCenterObservers() {
        NotificationCenter.default.removeObserver(mediaPlayerPlayObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerPauseObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerEndedObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerReadyObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerTimeUpdateObserver!)
        
        NotificationCenter.default.removeObserver(mediaPlayerFullscreenObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerPictureInPictureObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerBackgroundObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerForegroundObserver!)
        NotificationCenter.default.removeObserver(mediaPlayerSeekedObserver!)
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
        
        mediaPlayerTimeUpdateObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerTimeUpdate, object: nil, queue: nil,
                         using: mediaPlayerTimeUpdate)
        
        mediaPlayerFullscreenObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerFullscreen, object: nil, queue: nil,
                         using: mediaPlayerFullscreen)
        
        mediaPlayerPictureInPictureObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerPictureInPicture, object: nil, queue: nil,
                         using: mediaPlayerPictureInPicture)
        
        mediaPlayerBackgroundObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerBackground, object: nil, queue: nil,
                         using: mediaPlayerBackground)
        
        mediaPlayerForegroundObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerForeground, object: nil, queue: nil,
                         using: mediaPlayerForeground)

        mediaPlayerSeekedObserver = NotificationCenter.default
            .addObserver(forName: .mediaPlayerSeeked, object: nil, queue: nil,
                         using: mediaPlayerSeeked)
    }

}
