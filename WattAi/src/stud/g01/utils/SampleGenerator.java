package stud.g01.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class SampleGenerator {

    private static final String FILE_NAME = "./WattAi/resources/NpuzzleSamples.txt";

    // 接口函数，接收 size 和 num_of_samples 生成样例文件
    //生成num_of_samples个基本样例，size指定所有基本样例的格式
    //例如，size=3时，基本样例为：一行size，接下来size行是size*size的初始矩阵，之后size行是目标矩阵
    public void generateSamples(int size, int num_of_samples) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < num_of_samples; i++) {
                writer.write(String.valueOf(size));
                writer.newLine();
                int[][] matrix1 = generateRandomMatrix(size);
                int[][] matrix2 = generateRandomMatrix(size);
                writeMatrix(writer, matrix1);
                writeMatrix(writer, matrix2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 生成随机矩阵
    private int[][] generateRandomMatrix(int size) {
        int n = size * size;
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);

        int[][] matrix = new int[size][size];
        int index = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = numbers.get(index++);
            }
        }
        return matrix;
    }

    // 将矩阵写入文件
    private void writeMatrix(BufferedWriter writer, int[][] matrix) throws IOException {
        for (int[] row : matrix) {
            for (int val : row) {
                writer.write(val + " ");
            }
            writer.newLine();
        }
    }

    public static void main(String[] args) {
        // 示例用法
        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        SampleGenerator generator = new SampleGenerator();
        generator.generateSamples(3, 5); // 生成5个8数码问题样例
        //generator.generateSamples(4, 5); // 生成5个15数码问题样例
    }
}

