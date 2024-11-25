extension MediaPlayer {

    func addPlayers(playerId: String, player: MediaPlayerView) {
        players[playerId] = player
    }

    func removePlayer(playerId: String) {
        players.removeValue(forKey: playerId)
    }

    func removeAllPlayers() {
        players.removeAll()
    }

    func getPlayer(playerId: String) -> MediaPlayerView? {
        return players[playerId]
    }
}
