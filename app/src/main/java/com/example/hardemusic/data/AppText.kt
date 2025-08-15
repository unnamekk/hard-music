package com.example.hardemusic.data

object AppText {
    var language = "Es"

    val welcome: String
        get() = if (language == "Es") "Bienvenido!" else "Welcome!"

    val history: String
        get() = if (language == "Es") "Historial" else "History"

    val newSongs: String
        get() = if (language == "Es") "Nuevas canciones" else "New songs"

    val shuffle: String
        get() = if (language == "Es") "Aleatorio" else "Shuffle"

    val suggestions: String
        get() = if (language == "Es") "Sugerencias" else "Suggestions"

    val profilePlaceholder: String
        get() = "👤"

    val saveName: String
        get() = if (language == "Es") "Guardar nombre" else "Save name"

    val languageLabel: String
        get() = if (language == "Es") "Idioma" else "Language"

    val namePlaceholder: String
        get() = if (language == "Es") "Edita este campo!!!" else "Edit this field!!!"

    val songsTitle: String
        get() = if (language == "Es") "Canciones" else "Songs"
    val albumsTitle: String
        get() = if (language == "Es") "Álbumes" else "Albums"
    val artistsTitle: String
        get() = if (language == "Es") "Artistas" else "Artists"

    val noSongsTitle: String
        get() = if (language == "Es") "No hay canciones disponibles." else "There are no songs available"

    val nextQueueOption: String
        get() = if (language == "Es") "Poner como siguiente" else "Put as next"

    val goAlbumOption: String
        get() = if (language == "Es") "Ir a álbum" else "Go to album"

    val goArtistOption: String
        get() = if (language == "Es") "Ir a artista" else "Go to artist"

    val addPlaylistOption: String
        get() = if (language == "Es") "Agregar a una playlist" else "Add to a playlist"

    val editLabelOption: String
        get() = if (language == "Es") "Editar etiqueta" else "Edit Label"

    val deletePlaylistOption: String
        get() = if (language == "Es") "Eliminar de la playlist" else "Delete from the playlist"

    val deleteDeviceOption: String
        get() = if (language == "Es") "Eliminar del dispositivo" else "Delete from the device"

    val deleteDevice11Option: String
        get() = if (language == "Es") "Eliminar solo disponible en Android 11+" else "Delete only available on Android 11+"

    val selectPlaylistOption: String
        get() = if (language == "Es") "Selecciona una Playlist" else "Select a Playlist"

    val shuffelAlbumButton: String
        get() = if (language == "Es") "Reproducir Aleatoriamente" else "Play Randomly"

    val HistoryTitle: String
        get() = if (language == "Es") "Historial de hoy" else "Daily History"

    val newSongsTitle: String
        get() = if (language == "Es") "Nuevas canciones" else "New songs"

    val noPlaylistsTitle: String
        get() = if (language == "Es") "No tienes playlists" else "You don't have any playlists"

    val newPlaylistTitle: String
        get() = if (language == "Es") "Nueva Playlist" else "New Playlist"

    val namePlaylistPlaceholder: String
        get() = if (language == "Es") "Nombre de la playlist" else "Name of the playlist"

    val selectImageButton: String
        get() = if (language == "Es") "Seleccionar Imagen" else "Select Image"

    val createButton: String
        get() = if (language == "Es") "Crear" else "Create"

    val saveButton: String
        get() = if (language == "Es") "Guardar" else "Save"

    val cancelButton: String
        get() = if (language == "Es") "Cancelar" else "Cancel"

    val editPlaylistOption: String
        get() = if (language == "Es") "Editar Playlist" else "Edit Playlist"

    val deleteComPlaylistOption: String
        get() = if (language == "Es") "Eliminar Playlist" else "Delete Playlist"

    val addSongstoPlaylistTitle: String
        get() = if (language == "Es") "Agrega alguna canción a la playlist" else "Add some songs to the playlist"

    val SettingsTitle: String
        get() = if (language == "Es") "Configuración" else "Settings"

    val excludeAudios: String
        get() = if (language == "Es") "Excluir audios de WhatsApp" else "Exclude WhatsApp audios"

    val searchPlaceholder: String
        get() = if (language == "Es") "Buscar..." else "Search..."

    val loadingPlaceholder: String
        get() = if (language == "Es") "Cargando..." else "Loading..."

    val daysOfWeek: List<String>
        get() = if (language == "Es") {
            listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
        } else {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        }

    val editLabelScreen: String
        get() = if (language == "Es") "Editor de etiquetas" else "Label Editor"

    val fileRouteTitle: String
        get() = if (language == "Es") "Ruta de archivo" else "File route"

    val coverTitle: String
        get() = if (language == "Es") "Caratula" else "Cover"

    val useCoverButton: String
        get() = if (language == "Es") "Usar carátula del álbum" else "Use Album Cover"

    val songTitleTitle: String
        get() = if (language == "Es") "Título de la canción" else "Song Title"

    val artistsLabelTitle: String
        get() = if (language == "Es") "Artista(s)" else "Artist(s)"

    val albumLabelTitle: String
        get() = if (language == "Es") "Álbum" else "Album"

    val artistAlbumLabelTitle: String
        get() = if (language == "Es") "Artista del álbum" else "Artist Album"

    val yearLabelTitle: String
        get() = if (language == "Es") "Año" else "Year"

    val trackNumberLabelTitle: String
        get() = if (language == "Es") "Número de pista" else "Track Number"

    val mutipleEditionTitle: String
        get() = if (language == "Es") "Edición múltiple" else "Multiple Edition"

    val noChangeText: String
        get() = if (language == "Es") "Sin cambio" else "No Change"

    val newText: String
        get() = if (language == "Es") "Nuevo" else "New"

    val newImageText: String
        get() = if (language == "Es") "Nuevo (elegir imagen)" else "New(choose image)"

    val albumCoverText: String
        get() = if (language == "Es") "Imagen de álbum" else "Album Cover"

    val queueTitle: String
        get() = if (language == "Es") "Cola de reproducción" else "Reproduction Queue"

    val grantedPermissionsToast: String
        get() = if (language == "Es") "Permisos otorgados correctamente. Reiniciando la app" else "Permissions granted successfully. Restarting the app"

    val deniedPermissionsToast: String
        get() = if (language == "Es") "Permisos denegados. Cerrando aplicación" else "Permissions denied. Closing the app"

    val songDeletedToast: String
        get() = if (language == "Es") "Canción eliminada" else "Song deleted"

    val songNotDeletedToast: String
        get() = if (language == "Es") "No se pudo eliminar la canción" else "Song not deleted"

    val notEditionToast: String
        get() = if (language == "Es") "Permiso denegado para editar la canción" else "Permission denied to edit the song"

    val errorPermissionToast: String
        get() = if (language == "Es") "Error al solicitar permiso de edición" else "Error requesting permission to edit"

    val notEditionSongsToast: String
        get() = if (language == "Es") "Permiso denegado para editar canciones" else "Permission denied to edit the songs"

    val deletedSongToast: String
        get() = if (language == "Es") "Canción eliminada de la playlist" else "Song deleted from the playlist"

    val songQueueToast: String
        get() = if (language == "Es") "Canción agregada a la cola" else "Song added to the queue"

    val songSuccessToast: String
        get() = if (language == "Es") "Canción actualizada correctamente" else "Song updated successfully"

    val notSupportedToast: String
        get() = if (language == "Es") "Formato no soportado" else "Format not supported "

    val requiredPermissionToast: String
        get() = if (language == "Es") "Permiso requerido para editar" else "Permission required to edit"

    val errorUpdateToast: String
        get() = if (language == "Es") "Error al actualizar la canción" else "Error updating the song"

    val SuccessSongsToast: String
        get() = if (language == "Es") "Canciones actualizadas correctamente" else "Songs updated successfully"

    val errorUpdatesToast: String
        get() = if (language == "Es") "Error al actualizar canciones" else "Error updating songs"

    val english: String
        get() = if (language == "Es") "Ingles" else "English"

    val spanish: String
        get() = if (language == "Es") "Español" else "Spanish"
}