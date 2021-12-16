package com.renatsayf.stockinsider.ui.tracking

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.lifecycle.ViewModelProvider
import com.renatsayf.stockinsider.databinding.TrackingListFragmentBinding
import com.renatsayf.stockinsider.db.RoomSearchSet
import com.renatsayf.stockinsider.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackingListFragment : Fragment() {

    private val mainVM: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = TrackingListFragmentBinding.inflate(inflater, container, false)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {

                MaterialTheme {
                    Scaffold {
                        TrackerList(mainVM)
                    }
                }
            }
        }

        return binding.root
    }

    @Composable
    fun TrackerList(mainVM: MainViewModel) {

        val observableList by mainVM.getSearchSetList().observeAsState()
        val list = observableList
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            if (list != null) {
                items(list, null, itemContent = {
                    TrackerItem(set = it)
                })
            }
        }
    }

    @Composable
    fun TrackerItem(set: RoomSearchSet) {
        MaterialTheme {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                Card(
                    border = BorderStroke(Dp(1f), color = Color.Black),
                    modifier = Modifier.apply {
                        padding(12.dp)
                    }
                    ) {
                    Row {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = set.queryName, fontSize = 22.sp)

                            val dealType = if (set.isPurchase && !set.isSale) "Покупка"
                            else if (set.isSale && !set.isPurchase) "Продажа"
                            else "Покупка и продажа"

                            Text(text = dealType, fontSize = 10.sp)
                            Row {
                                Text("Отслеживать")
                                Switch(checked = true, onCheckedChange = {

                                })
                            }
                        }
                        IconButton(modifier = Modifier.fillMaxSize(32.0f), onClick = { /*TODO*/ }) {
                            Icon(Icons.Filled.Delete, contentDescription = "")
                        }
                    }
                }
            }
        }
    }

}