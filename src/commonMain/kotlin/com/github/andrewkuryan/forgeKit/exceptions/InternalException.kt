package com.github.andrewkuryan.forgeKit.exceptions

import com.github.andrewkuryan.forgeKit.execution.ExecState

class InternalException(val execState: ExecState, val position: Int) :
    Exception("[Internal Error]: Something went wrong on position $position")