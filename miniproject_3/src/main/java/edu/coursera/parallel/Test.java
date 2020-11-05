package edu.coursera.parallel;

import java.util.Arrays;

public class Test {
  public static int N = 10;

  static double[] stencilComputation(int iteration){
    double[] arr = new double[N];
    arr[0] = 0.0;
    arr[N - 1] = 1.0;

    double[] oldArr = arr;
    double[] newArr = Arrays.copyOfRange(oldArr, 0, N);

    for(int i = 0; i < iteration; i++){
      for(int j = 1; j < N - 1; j++){
        newArr[j] = (oldArr[j - 1] + oldArr[j + 1]) / 2.0;
      }
      double[] temptArr = newArr;
      newArr = oldArr;
      oldArr = temptArr;
    }

    return arr;
  }

  public static void main(String[] args) {
    double[] doubles = stencilComputation(100000);
    System.out.println(Arrays.toString(doubles));
  }
}