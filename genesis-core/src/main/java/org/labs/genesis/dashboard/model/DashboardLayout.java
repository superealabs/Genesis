package org.labs.genesis.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardLayout {
    private int x;
    private int y;
    private int width;
    private int height;

    public int getCol() { return x; }
    public void setCol(int col) { this.x = Math.max(0, col); }
    public int getRow() { return y; }
    public void setRow(int row) { this.y = Math.max(0, row); }

    @Override
    public String toString() {
        return "DashboardLayout { x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + " }";
    }
}
