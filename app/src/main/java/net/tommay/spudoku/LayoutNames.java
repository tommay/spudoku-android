package net.tommay.spudoku;

import java.util.List;

import net.tommay.sudoku.Layout;
import scala.collection.JavaConversions;

class LayoutNames {
    public static List<String> getLayoutNames() {
        return JavaConversions.seqAsJavaList(Layout.getLayoutNames().toSeq());
    }
}
