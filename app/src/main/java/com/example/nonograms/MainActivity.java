package com.example.nonograms;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private final int BOARD_ROWS = 5;
    private final int BOARD_COLUMNS = 5;
    private boolean[][] blackSquares = new boolean[BOARD_ROWS][BOARD_COLUMNS];
    private int lives = 3; // 생명력 초기값
    private boolean markBlack = true; // 클릭 모드: true = 검정 칸 표시, false = X 표시
    private TextView livesTextView; // 생명력 표시

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ScrollView로 전체 레이아웃을 감싸기
        ScrollView scrollView = new ScrollView(this);

        // 메인 레이아웃 (수직 배치)
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // 상단 UI 레이아웃
        LinearLayout topLayout = new LinearLayout(this);
        topLayout.setOrientation(LinearLayout.HORIZONTAL);
        topLayout.setGravity(Gravity.CENTER);
        topLayout.setPadding(10, 10, 10, 10);

        // 생명력 표시
        livesTextView = new TextView(this);
        livesTextView.setText("생명력: " + lives);
        livesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        livesTextView.setPadding(20, 0, 20, 0);
        topLayout.addView(livesTextView);

        // 클릭 모드 전환 ToggleButton
        ToggleButton toggleButton = new ToggleButton(this);
        toggleButton.setTextOn("검정 칸 표시");
        toggleButton.setTextOff("X 표시");
        toggleButton.setChecked(true); // 기본값: 검정 칸 표시
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> markBlack = isChecked);
        topLayout.addView(toggleButton);

        // TableLayout (게임 보드)
        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setStretchAllColumns(true);

        // 검정 칸 랜덤 설정
        initializeBlackSquares();

        // 셀 크기(dp 단위)
        int cellSizeInDp = 30; // 셀 크기를 30dp로 줄임 (가로/세로 동일)

        // 첫 3줄: 열 힌트 표시
        for (int i = 0; i < 3; i++) {
            TableRow tableRow = new TableRow(this);
            for (int j = 0; j < 8; j++) {
                TextView textView = new TextView(this);

                if (j >= 3) {
                    String columnHint = i < getColumnHints(j - 3).length ? String.valueOf(getColumnHints(j - 3)[i]) : "";
                    textView.setText(columnHint);
                    textView.setBackgroundColor(android.graphics.Color.LTGRAY);
                } else {
                    textView.setText("");
                    textView.setBackgroundColor(android.graphics.Color.WHITE);
                }

                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                textView.setWidth(dpToPx(cellSizeInDp));
                textView.setHeight(dpToPx(cellSizeInDp));
                tableRow.addView(textView);
            }
            tableLayout.addView(tableRow);
        }

        // 나머지 5줄: 행 힌트와 셀 추가
        for (int i = 0; i < BOARD_ROWS; i++) {
            TableRow tableRow = new TableRow(this);

            // 행 힌트 추가
            int[] rowHints = getRowHints(i);
            for (int j = 0; j < 3; j++) {
                TextView textView = new TextView(this);
                textView.setText(j < rowHints.length ? String.valueOf(rowHints[j]) : "");
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                textView.setBackgroundColor(android.graphics.Color.LTGRAY);
                textView.setWidth(dpToPx(cellSizeInDp));
                textView.setHeight(dpToPx(cellSizeInDp));
                tableRow.addView(textView);
            }

            // 셀 추가
            for (int j = 0; j < BOARD_COLUMNS; j++) {
                Cell cell = new Cell(this);
                cell.setLayoutParams(new TableRow.LayoutParams(dpToPx(cellSizeInDp), dpToPx(cellSizeInDp)));

                final int rowIndex = i;
                final int colIndex = j;

                cell.setOnClickListener(v -> {
                    if (markBlack) { // 검정 칸 표시 모드
                        if (blackSquares[rowIndex][colIndex]) {
                            if (cell.markBlackSquare()) {
                                checkGameCompletion();
                            }
                        } else {
                            decreaseLives();
                        }
                    } else { // X 표시 모드
                        cell.toggleX();
                    }
                });

                tableRow.addView(cell);
            }

            tableLayout.addView(tableRow);
        }

        // 레이아웃에 추가
        mainLayout.addView(topLayout);
        mainLayout.addView(tableLayout);
        scrollView.addView(mainLayout);

        setContentView(scrollView);
    }

    // dp를 px로 변환하는 헬퍼 메서드
    private int dpToPx(int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
    }

    // 생명력 감소
    private void decreaseLives() {
        lives--;
        livesTextView.setText("생명력: " + lives);
        if (lives == 0) {
            endGame(false);
        }
    }

    // 검정 칸 랜덤 초기화
    private void initializeBlackSquares() {
        for (int i = 0; i < BOARD_ROWS; i++) {
            for (int j = 0; j < BOARD_COLUMNS; j++) {
                blackSquares[i][j] = Math.random() < 0.5;
            }
        }
    }

    // 행 힌트 계산
    private int[] getRowHints(int rowIndex) {
        return calculateHints(blackSquares[rowIndex]);
    }

    // 열 힌트 계산
    private int[] getColumnHints(int columnIndex) {
        boolean[] column = new boolean[BOARD_ROWS];
        for (int i = 0; i < BOARD_ROWS; i++) {
            column[i] = blackSquares[i][columnIndex];
        }
        return calculateHints(column);
    }

    // 힌트 계산
    private int[] calculateHints(boolean[] line) {
        java.util.ArrayList<Integer> hints = new java.util.ArrayList<>();
        int count = 0;

        for (boolean square : line) {
            if (square) {
                count++;
            } else if (count > 0) {
                hints.add(count);
                count = 0;
            }
        }
        if (count > 0) hints.add(count);

        return hints.stream().mapToInt(i -> i).toArray();
    }

    // 게임 완료 확인
    private void checkGameCompletion() {
        if (Cell.getNumBlackSquares() == 0) {
            endGame(true);
        }
    }

    // 게임 종료 처리
    private void endGame(boolean win) {
        String message = win ? "게임 클리어! 축하합니다!" : "게임 오버! 다시 도전하세요.";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // 모든 셀 비활성화 (추가 구현 필요)
        // 셀들의 참조를 저장하여 반복문으로 클릭 비활성화 처리
    }
}
