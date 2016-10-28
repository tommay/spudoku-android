package net.tommay.spudoku;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import net.tommay.spudoku.Puzzle;

public class MainActivity extends AppCompatActivity {

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
            Integer color = puzzle.getColor(i);
            if (color != null) {
                cell.setImageResource(R.drawable.circle);
            }
            else {
                cell.setImageResource(R.drawable.empty);
            }
        }
    }

    public void clicked(View view) {
        String tag = (String)view.getTag();
        Log.i("Spudoku", "clicked " + tag);

        ImageView cell = (ImageView)view;

        Puzzle puzzle = _puzzles[_puzzle];
        int n = Integer.parseInt(tag);

        _puzzles[_puzzle].flip(n);

        Integer color = puzzle.getColor(n);
        if (color != null) {
            cell.setImageResource(R.drawable.circle);
        }
        else {
            cell.setImageResource(R.drawable.empty);
        }
    }

    public void clickNew(View view) {
        Log.i("Spudoku", "new");
    }
}
