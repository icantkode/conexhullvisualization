package com.asher.convexhulls;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import java.util.*;

public class ConvexHullUtil {
    static boolean shouldContinue = true;
    static int maxY;
    static int maxIdx = -1;
    static point maxPoint;
    static Stack<point> stack = new Stack<>();
    static point last;
    static point secondLast;
    static Stack<Line> lines = new Stack<>();
    private static int ccwSlope(point p0, point p1, point p2){
        int dx1,dx2,dy1,dy2;
        dx1 = p1.x - p0.x;
        dy1 = p1.y - p0.y;
        dx2 = p2.x - p0.x;
        dy2 = p2.y - p0.y;
        if(dx1*dy2 > dy1*dx2){
            return 1;
        }
        if (dx1*dy2 < dy1*dx2){
            return -1;
        }
        if ( (dx1*dx2 < 0) || (dy1*dy2 < 0) ){
            return -1;
        }
        if( (dx1*dx1 + dy1*dy1) < (dx2*dx2 + dy2*dy2) ){
            return 1;
        }
        return 0;
    }
    public static boolean doIntersectSlope(point p0, point p1, point p2, point p3){
        return (ccwSlope(p0,p1,p2)*ccwSlope(p0,p1,p3) <= 0) && (ccwSlope(p2,p3,p0)*ccwSlope(p2,p3,p1) <= 0);
    }
    private static int ccwArea(point p0, point p1, point p2){
        int area = (p1.x - p0.x)*(p2.y-p0.y) - (p1.y-p0.y)*(p2.x-p0.x);
        if(area<0){
            return -1;
        }else if(area>0){
            return 1;
        }else{
            return 0;
        }
    }
    public static boolean doIntersectArea(point p0, point p1, point p2, point p3){
        return (ccwArea(p0,p1,p2)*ccwArea(p0,p1,p3) <= 0) && (ccwArea(p2,p3,p0)*ccwArea(p2,p3,p1) <= 0);
    }
    public static float angle(point p1, point p2){
        int dx = p2.x - p1.x;
        int dy = p2.y - p1.y;
        int ax = Math.abs(dx);
        int ay = Math.abs(dy);
        float angle = (ax+ay==0) ? 0 : (float) dy/(ax+ay);
        if(dx<0){
            angle = 2 - angle;
        } else if (dy<0) {
            angle = 4 + angle;
        }
        return angle*90.0f;
    }
    public static void bruteForce(List<point> points, Stage stg, Group grp){
        stg.getScene().setOnKeyPressed(null);
        for(int i = 0 ; i < points.size() ; i++){
            for (int j = 0 ; j < points.size() ; j++){
                if(points.get(i)!=points.get(j)){
                    //draw temp line ij
                    for(int k = 0 ; k < points.size() ; k++){
                        shouldContinue = true;
                        if(points.get(k) != points.get(j) && points.get(k)!=points.get(i)){
                            //draw line jk
                            if(ccwSlope(points.get(i),points.get(j),points.get(k))==-1){
                                //remove line jk
                                shouldContinue = false;
                            }
                            //remove line ij
                            if(!shouldContinue){
                                break;
                            }
                        }
                    }
                }
                if(shouldContinue){
                    //perma line ij
                    Line l = new Line(points.get(i).x, points.get(i).y, points.get(j).x, points.get(j).y);
                    l.setStroke(Color.web("#008000"));
                    grp.getChildren().add(l);
                }
            }
        }
    }
    private static void findMaxY(List<point> points) {
        maxY = points.get(0).y;
        maxIdx = 0;
        maxPoint = points.get(0);
        for (int i = 1; i < points.size(); i++) {
            if (points.get(i).y > maxY) {
                maxY = points.get(i).y;
                maxIdx = i;
                maxPoint = points.get(i);
            }
        }
    }
    private static float myAngle(point a, point b){
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        double angle = Math.atan2(dy,dx);
        if(Math.toDegrees(angle)<0){
            return (float) (360.0f + Math.toDegrees(angle));
        }else{
            return (float) Math.toDegrees(angle);
        }
    }
    static LinkedList<point> hull = new LinkedList<>();
    private static float currAngle = 0.0f;
    private static int index=1;
    public static void jarvis2(List<point> points, Stage stg, Group grp){
        stg.getScene().setOnKeyPressed(null);
        findMaxY(points);
        points.set(maxIdx, points.get(index));
        points.set(index, maxPoint);
        for(int i = 0 ; i < points.size() ;i++){
            points.get(i).isOnHull=false;
            points.get(i).isFirstPointOnHull=false;
        }
        hull.add(points.get(index));
        points.get(1).isOnHull = true;
        points.get(1).isFirstPointOnHull = true;
        while( ! (hull.get(0)==hull.get(hull.size()-1) && hull.size()>=3) ){
            for(int j = 0 ; j < points.size() ; j++){
                if(points.get(index)==points.get(j)){
                    points.get(j).angle = 0.0f;
                }else if(points.get(j).isOnHull && !points.get(j).isFirstPointOnHull){
                    points.get(j).angle = Float.MAX_VALUE;
                }else{
                    points.get(j).angle = myAngle(points.get(index), points.get(j));
                }
            }
            points.sort(Comparator.comparingDouble(point::getAngle));
            index = 1;
            for (int i = 1 ; i < points.size() ; i++){
                if( ((points.get(i).isOnHull && points.get(i).isFirstPointOnHull)||(!points.get(i).isOnHull)) && points.get(i).angle >= currAngle){
                    points.get(i).isOnHull = true;
                    hull.add(points.get(i));
                    currAngle = points.get(i).angle;
                    Line l = new Line(points.get(0).x, points.get(0).y, points.get(i).x, points.get(i).y);
                    l.setStroke(Color.web("#008000"));
                    grp.getChildren().add(l);
                    break;
                }else{
                    index++;
                }
            }
        }
    }
    public static void grahamScan(List<point> points, Stage stg, Group grp){
        stg.getScene().setOnKeyPressed(null);
        findMaxY(points);
        points.set(maxIdx, points.get(0));
        points.set(0, maxPoint);
        for(int i = 0 ; i < points.size() ; i++){points.get(i).angle = myAngle(points.get(0), points.get(i));}
        points.sort(Comparator.comparingDouble(point::getAngle));
        stack.push(points.get(0));
        secondLast = points.get(0);
        stack.push(points.get(1));
        last = points.get(1);
        drawLine(last, secondLast, grp);

        for (int i = 2 ; i < points.size() ; i++){
            if(ccwSlope(secondLast, last, points.get(i)) == 1 || ccwSlope(secondLast, last, points.get(i)) == 0 ){
                stack.push(points.get(i));
                secondLast = last;
                last = points.get(i);
                drawLine(last, secondLast, grp);
            }
            else{
                grp.getChildren().remove(lines.pop());
                point j = stack.pop();
                last = secondLast;
                secondLast = stack.get(stack.size()-2);
                i--;
            }
        }
        stack.add(stack.get(0));
        last = stack.get(stack.size()-1);
        secondLast = stack.get(stack.size()-2);
        drawLine(last,secondLast,grp);
    }
    private static void drawLine(point a, point b, Group grp){
        Line l1 = new Line(a.x, a.y, b.x, b.y);
        l1.setStroke(Color.web("#008000"));
        grp.getChildren().add(l1);
        lines.push(l1);
    }
}
