package com.github.andrewkuryan.forge.parserKit.exceptions

import com.github.andrewkuryan.forge.parserKit.execution.ExecState

class InternalException(val execState: ExecState, val position: Int) :
    Exception("[Internal Error]: Something went wrong on position $position")