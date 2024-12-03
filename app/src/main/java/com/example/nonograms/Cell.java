package com.example.nonograms;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

public class Cell extends AppCompatButton {
    private boolean blackSquare; // 검정 칸 여부
    private boolean checked;     // "X" 여부
    private static int numBlackSquares = 0; // 남은 검정 칸 수

    public Cell(@NonNull Context context) {
        super(context);
        this.setWidth(150);
        this.setHeight(150);
        this.setBackgroundColor(Color.WHITE);

        // 50% 확률로 검정 칸 설정
        if (Math.random() < 0.5) {
            blackSquare = true;
            numBlackSquares++;
        }
    }

    public boolean isBlackSquare() {
        return blackSquare;
    }

    public static int getNumBlackSquares() {
        return numBlackSquares;
    }

    public boolean markBlackSquare() {
        if (checked) return false; // 이미 "X" 표시된 경우

        if (blackSquare) {
            setBackgroundColor(Color.BLACK);
            setClickable(false);
            numBlackSquares--;
            return true;
        } else {
            setBackgroundColor(Color.RED); // 잘못된 클릭
            return false;
        }
    }

    public void toggleX() {
        if (!checked) {
            setText("X");
            setTextColor(Color.BLUE);
            checked = true;
        } else {
            setText("");
            checked = false;
        }
    }
}
