package com.renatsayf.stockinsider.ui.about_app

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.renatsayf.stockinsider.R
import com.renatsayf.stockinsider.databinding.FragmentLogsBinding
import com.renatsayf.stockinsider.utils.LOGS_FILE_NAME
import com.renatsayf.stockinsider.utils.isLogsFileExists
import com.renatsayf.stockinsider.utils.showSnackBar


class LogsFragment : Fragment() {

    private lateinit var binding: FragmentLogsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {

            tvLogs.movementMethod = ScrollingMovementMethod()

            requireContext().isLogsFileExists(
                LOGS_FILE_NAME,
                onExists = { file ->
                    val text = file.readText()
                    tvLogs.text = text
                },
                onNotExists = {
                    showSnackBar("$LOGS_FILE_NAME not found")
                }
            )

            toolBar.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when(menuItem.itemId) {
                        R.id.menu_clear_logs -> {
                            requireContext().isLogsFileExists(
                                LOGS_FILE_NAME,
                                onExists = {file ->
                                    file.writeText("********************")
                                    val text = file.readText()
                                    tvLogs.text = text
                                }
                            )
                        }
                    }
                    return false
                }
            }, viewLifecycleOwner)
        }

    }

    override fun onResume() {
        super.onResume()

        binding.toolBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })
    }


}