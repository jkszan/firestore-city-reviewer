package com.google.firebase.sample.cityreviewer.kotlin.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.sample.cityreviewer.kotlin.Filters

/**
 * ViewModel for [com.google.firebase.sample.cityreviewer.MainActivity].
 */

class MainActivityViewModel : ViewModel() {

    var isSigningIn: Boolean = false
    var filters: Filters = Filters.default
}
