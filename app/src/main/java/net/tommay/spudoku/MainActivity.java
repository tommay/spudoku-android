package net.tommay.spudoku;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import net.tommay.spudoku.Puzzle;

public class MainActivity extends AppCompatActivity {
    private int _emptyCellColor;

    private Puzzle[] _puzzles = new Puzzle[] {
        new Puzzle(
            "579821463231467598684359217167235984342978651895146372456782139928613745713594826",
            "001111000111111010011100111001100110011101110011001100111001110010111111000111100"),

        new Puzzle(
            "238915647156734982794628513682479351543162879917583426479256138861347295325891764",
            "011100101011101011011011100010110111100010001111011010001110110110101110101001110"),
    };

    private int _puzzle = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("Spudoku", "onCreate");
        Resources res = getResources();
        _emptyCellColor = res.getColor(R.color.emptyCell);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Spudoku", "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Spudoku", "onResume");
        setup();
    }

    // This is pretty awful.

    private void setup () {
        Puzzle puzzle = _puzzles[_puzzle];
        View view = findViewById(R.id.board);
        for (int i = 0; i < 81; i++) {
            ImageView cell =
                (ImageView)view.findViewWithTag(Integer.toString(i));
            colorCell(cell, puzzle, i);
        }
    }

    private void colorCell(ImageView cell, Puzzle puzzle, int n)
    {
        GradientDrawable drawable = (GradientDrawable)cell.getDrawable();
        Log.i("Spudoku", "drawable: " + drawable);

        Integer color = puzzle.getColor(n);
        int c = color != null ? color : _emptyCellColor;
        drawable.setColor(0xFF000000 | c);
    }

    public void clicked(View view) {
        String tag = (String)view.getTag();
        Log.i("Spudoku", "clicked " + tag);

        Puzzle puzzle = _puzzles[_puzzle];
        int n = Integer.parseInt(tag);

        _puzzles[_puzzle].flip(n);

        colorCell((ImageView)view, puzzle, n);
    }

    public void clickNew(View view) {
        Log.i("Spudoku", "new");
    }

    public void clickSetup(View view) {
        Log.i("Spudoku", "setup");
    }

    public void clickSolved(View view) {
        Log.i("Spudoku", "solved");
    }
}
