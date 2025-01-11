extension MediaPlayer {
    
    func addMediaPlayerController(playerId: String, controller: MediaPlayerController) {
        controllers[playerId] = controller
    }

    func removeMediaPlayerController(playerId: String) {
        controllers.removeValue(forKey: playerId)
    }

    func removeAllMediaPlayerControllers() {
        controllers.removeAll()
    }

    func getMediaPlayerController(playerId: String) -> MediaPlayerController? {
        return controllers[playerId]
    }
}
