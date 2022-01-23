package info.mqtt.android.extsample.internal

import android.content.Context
import info.mqtt.android.extsample.room.AppDatabase
import info.mqtt.android.extsample.room.PersistenceDao
import info.mqtt.android.extsample.utils.toConnection
import info.mqtt.android.extsample.utils.toConnectionEntity
import timber.log.Timber


class Connections private constructor(context: Context) {

    var connections: HashMap<String, Connection> = HashMap()

    private var persistence: PersistenceDao = AppDatabase.getDatabase(context).persistenceDao()

    init {
        val connectionDaoList = persistence.all.map { it.toConnection(context) }
        connectionDaoList.forEach {
            Timber.d("Connection was persisted.. ${it.handle()}")
            connections[it.handle()] = it
        }
    }

    fun getConnection(handle: String): Connection? {
        return connections[handle]
    }

    fun addConnection(connection: Connection) {
        connections[connection.handle()] = connection
        persistence.insert(connection.toConnectionEntity())
    }

    fun removeConnection(connection: Connection) {
        connections.remove(connection.handle())
        persistence.delete(connection.toConnectionEntity())
    }

    fun updateConnection(connection: Connection) {
        connections[connection.handle()] = connection
        persistence.updateAll(connection.toConnectionEntity())
    }

    companion object {
        private var instance: Connections? = null

        @Synchronized
        fun getInstance(context: Context): Connections {
            if (instance == null) {
                instance = Connections(context)
            }
            return instance!!
        }
    }
}
