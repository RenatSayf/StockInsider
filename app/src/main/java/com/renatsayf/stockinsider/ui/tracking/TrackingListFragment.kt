package com.renatsayf.stockinsider.ui.tracking

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import com.renatsayf.stockinsider.databinding.TrackingListFragmentBinding

class TrackingListFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = TrackingListFragmentBinding.inflate(inflater, container, false)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {

                }
            }
        }

        return binding.root
    }

    @Composable
    fun TrackerList(name: String = "") {

    }

    @Preview(name = "Tracking item")
    @Composable
    fun TrackerItem() {
        MaterialTheme {
            Row {
                Card(
                    border = BorderStroke(Dp(1f), color = Color.Black),
                    modifier = Modifier.apply {
                        padding(12.dp)
                    }
                    ) {
                    Row {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "NASDAQ голубые фишки", fontSize = 22.sp)
                            Text(text = "Покупка и продажа", fontSize = 10.sp)
                            Row {
                                Text("Отслеживать")
                                Switch(checked = true, onCheckedChange = {})
                            }
                        }
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Filled.Delete, contentDescription = "")
                        }
                    }
                }
            }
        }
    }

}