package com.notamethod.fluppy.util;

import javafx.geometry.Point2D;
import org.junit.jupiter.api.Test;

public class CoordinatesTest {

    @Test
    void matchingTest() {
        int x=50;
        int y=50;
        Point2D p2d = new Point2D(100,100);

        p2d.add(x,20d);


        x=-50;
         y=-50;
        Point2D p4d =p2d.add(x,y);
        Point2D p3d = new Point2D(p2d.getX()-10,p2d.getY()-10);


    }
}
