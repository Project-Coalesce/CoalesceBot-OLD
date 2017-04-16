package com.coalesce

import com.google.common.base.Preconditions

fun main(args: Array<String>) {
    Preconditions.checkArgument(args.size == 1, "Please enter a valid token.")
    Bot().run(args[0])
}