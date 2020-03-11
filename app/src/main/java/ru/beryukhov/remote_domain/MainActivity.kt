package ru.beryukhov.remote_domain

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.beryukhov.common.model.Post
import ru.beryukhov.common.model.User
import ru.beryukhov.remote_domain.push.OkHttpPush
import ru.beryukhov.remote_domain.recycler.DomainListAdapter
import ru.beryukhov.remote_domain.recycler.UserItem

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupNetworkButton()
        setupDatabaseButton()
        setupSocketButton()
        setupCreateDbButton()
    }

    private fun setupNetworkButton() {
        val button: Button = findViewById(R.id.button_http)
        button.setOnClickListener {
            testHttp(::log)
        }
    }

    private fun setupDatabaseButton() {
        val button: Button = findViewById(R.id.button_db)
        button.setOnClickListener {
            testDb(this, ::log)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun setupSocketButton() {
        val broadcastChannel = BroadcastChannel<Any>(Channel.CONFLATED)

        val button: Button = findViewById(R.id.button_socket)
        val push = OkHttpPush()
        val gson = Gson()
        button.setOnClickListener {
            push.startReceive(socketUrl = SOCKET_URL, log = ::log) {
                try {
                    //{"method":"Create","entity":"Post"}
                    val apiRequest =
                        gson.fromJson<ApiRequest>(it.toString(), ApiRequest::class.java)
                    //todo change User and Post instanses by corresponding type flags
                    when (apiRequest.entity) {
                        "User" -> broadcastChannel.offer(User("", ""))
                        "Post" -> broadcastChannel.offer(Post("", "", ""))
                    }
                } catch (e: JsonSyntaxException) {
                    Log.i("MainActivity", "JsonSyntaxException $e")
                } catch (e: RuntimeException) {
                    Log.i("MainActivity", "RuntimeException $e")
                }
            }
        }

        GlobalScope.launch {
            broadcastChannel.consumeEach {
                log("event got $it")
                when (it) {
                    is User -> {/*http<User>->db*/
                    }
                    is Post -> {/*http<Post>->db*/
                    }
                }
            }
        }
    }

    private fun setupCreateDbButton() {
        val button: Button = findViewById(R.id.button_create_db)
        val adapter = setupRecycler()

        val dbRepo = DatabaseRepository(this, ::log)

        button.setOnClickListener {
            dbRepo.createDb()
            dbRepo.getUserFlow().onEach {
                Log.d("DR_", "onEach")
                log(it.toString())

                withContext(Dispatchers.Main) {
                    adapter.clearAll()
                    adapter.add(it
                        .map { dbUser -> User(dbUser.id, dbUser.user_name) }
                        .map { item -> UserItem(item) }
                    )
                }
            }.launchIn(CoroutineScope(Dispatchers.Default))


            //todo remove
            GlobalScope.launch {
                for (i in 1..8) {
                    Log.d("DR_", "emit $i")
                    delay(2000)
                    dbRepo.insertUser(
                        ru.beryukhov.remote_domain.User.Impl(
                            "user_id_$i",
                            "user_name_$i"
                        )
                    )
                }
            }
        }
    }

    private fun setupRecycler(): DomainListAdapter {
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        val adapter = DomainListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        return adapter
    }


    private suspend fun log(s: String) {
        Log.i("MainActivity", s)
        /*withContext(Dispatchers.Main) {
            val textView: TextView = findViewById(R.id.textView)
            textView.text = "${textView.text}\n$s"
        }*/
    }

}

data class ApiRequest(val method: String, val entity: String)
