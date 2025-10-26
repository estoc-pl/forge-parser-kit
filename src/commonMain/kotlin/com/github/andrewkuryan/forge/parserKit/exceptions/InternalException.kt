package com.github.andrewkuryan.forge.parserKit.exceptions

import com.github.andrewkuryan.forge.parserKit.execution.Situation

class InternalException(situation: Situation, position: Int) :
    Exception("[Internal Error]: Something went wrong on position $position with situation: $situation")