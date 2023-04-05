package com.davidp799.patcotoday

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private var _internet = false
    private var _updated = false
    private var _downloaded = false
    private var _extracted = false

    var internet = _internet
    var updated = _updated
    var downloaded = _downloaded
    var extracted = _extracted

}
