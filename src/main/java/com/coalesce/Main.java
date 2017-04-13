package com.coalesce;

import com.google.common.base.Preconditions;

public class Main {
    public static void main(String args[]) throws Exception {
        Preconditions.checkArgument(args.length == 1, "Token has to be specified.");
        new Bot().run(args[0]);
    }
}
