package sirs.spykid.guardian.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import sirs.spykid.guardian.R
import sirs.spykid.util.Child
import sirs.spykid.util.listChildren
import java.util.*
import java.util.function.Consumer

@RequiresApi(api = Build.VERSION_CODES.N)
class MenuActivity : AppCompatActivity() {

    private var listView: ListView? = null
    private val children = ArrayList<PrettyChild>()
    private var error: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        error = findViewById(R.id.menu_error)
        findViewById<Button>(R.id.add_beacon_button).setOnClickListener {
            val intent = Intent(applicationContext, AddBeaconActivity::class.java)
            startActivityForResult(intent, 0)
            listChildren()
        }
        findViewById<Button>(R.id.refresh_button).setOnClickListener { listChildren() }

        listChildren()
        listView = findViewById(R.id.beacon_list)
        listView!!.setOnItemClickListener { _, _, position, _ ->
            val child = children[position].child
            val intent = Intent(applicationContext, MapsActivity::class.java)
            intent.putExtra("child", child)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        listChildren()
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun listChildren() {
        listChildren(Consumer { r ->
            r.match(
                Consumer { ok ->
                    runOnUiThread {
                        children.clear()
                        children.addAll(
                            ok.children.map { PrettyChild(it) }.toList()
                        )
                        listView!!.adapter =
                            ArrayAdapter(this, android.R.layout.simple_list_item_1, children)
                    }
                },
                Consumer { err -> runOnUiThread { error!!.text = "Error listing children: $err" } }
            )
        })
    }

    private data class PrettyChild internal constructor(internal val child: Child) {
        override fun toString(): String {
            return child.username
        }
    }
}
