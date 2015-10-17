import java.io.*;
import java.util.*;
import java.lang.Math;

public class SortingTest {
    public static void main(String args[]) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        try {
            boolean isRandom = false; // 입력받은 배열이 난수인가 아닌가?
            int[] value; // 입력 받을 숫자들의 배열
            String nums = br.readLine();
            if (nums.charAt(0) == 'r') {
                // 난수일 경우
                isRandom = true;

                String[] nums_arg = nums.split(" ");

                int numsize = Integer.parseInt(nums_arg[1]); // 총 갯수
                int rminimum = Integer.parseInt(nums_arg[2]); // 최소값
                int rmaximum = Integer.parseInt(nums_arg[3]); // 최대값

                Random rand = new Random();

                value = new int[numsize];
                for (int i = 0; i < value.length; i++)
                    value[i] = rand.nextInt(rmaximum - rminimum + 1) + rminimum;
            } else {
                // 난수가 아닐 경우
                int numsize = Integer.parseInt(nums);

                value = new int[numsize];
                for (int i = 0; i < value.length; i++)
                    value[i] = Integer.parseInt(br.readLine());
            }

            while (true) {
                int[] newvalue = (int[]) value.clone(); // 원래 값의 보호를 위해 copy를 생성한다.

                String command = br.readLine();

                long t = System.currentTimeMillis();
                switch (command.charAt(0)) {
                case 'B': // Bubble Sort
                    newvalue = DoBubbleSort(newvalue);
                    break;
                case 'I': // Insertion Sort
                    newvalue = DoInsertionSort(newvalue);
                    break;
                case 'H': // Heap Sort
                    newvalue = DoHeapSort(newvalue);
                    break;
                case 'M': // Merge Sort
                    newvalue = DoMergeSort(newvalue);
                    break;
                case 'Q': // Quick Sort
                    newvalue = DoQuickSort(newvalue);
                    break;
                case 'R': // Radix Sort
                    newvalue = DoRadixSort(newvalue);
                    break;
                case 'X':
                    return; // 프로그램을 종료한다.
                default:
                    throw new IOException("잘못된 정렬 방법을 입력했습니다.");
                }
                if (isRandom) {
                    // 난수일 경우 수행 시간을 출력한다.
                    System.out.println((System.currentTimeMillis() - t) + " ms");
                } else {
                    // 난수가 아닐 경우 정렬된 결과값을 출력한다.
                    for (int i = 0; i < newvalue.length; i++) {
                        System.out.println(newvalue[i]);
                    }
                }

            }
        } catch (IOException e) {
            System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int[] DoBubbleSort(int[] value) {
        int arrSize = value.length;
        int count = 0;

        for (int i = 0; i < arrSize - 1; i++) {
            for (int j = 0; j < arrSize - 1 - i; j++) {
                if (value[j] > value[j + 1])
                    swap(value, j, j + 1);
                else
                    count++;
            }

            if (i == 0 && arrSize - 1 == count)
                break;
        }

        return (value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int[] DoInsertionSort(int[] value) {
        int arrSize = value.length;

        for (int i = 1; i < arrSize; i++) {
            int j = i - 1;
            int temp = value[i];

            while (j >= 0 && value[j] > temp) {
                value[j + 1] = value[j];
                j--;
            }
            value[j + 1] = temp;
        }

        return (value);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int[] DoHeapSort(int[] value) {
        // 2 * i + 1 -> left child
        // 2 * i + 2 -> right child
        // (i + 1) / 2 - 1 -> parent (if i is int data type)

        int arrSize = value.length;

        for (int i = 0; i < arrSize; i++)
            makeHeap(value, i);

        for (int i = arrSize - 1; i > 0; i--)
            heapSort(value, i);

        return (value);
    }

    private static void makeHeap(int[] value, int currIndex) { // o(n)
        int rootIndex = 0;
        int parentIndex;

        while (currIndex > rootIndex) {
            parentIndex = (currIndex + 1) / 2 - 1;

            if (value[currIndex] <= value[parentIndex])
                break;
            else {
                swap(value, parentIndex, currIndex);

                currIndex = parentIndex;
            }
        }
    }

    private static void heapSort(int[] value, int lastIndex) { // o(nlogn)
        // http://en.wikipedia.org/wiki/Heapsort#Pseudocode의 Pseudo Code 참고
        int currIndex = 0;
        int leftIndex;
        int rightIndex;
        int temp;

        swap(value, 0, lastIndex);

        while (currIndex * 2 + 1 <= lastIndex - 1) {
            leftIndex = currIndex * 2 + 1;
            temp = currIndex;
            rightIndex = leftIndex + 1;

            if (value[temp] < value[leftIndex])
                temp = leftIndex;
            if (rightIndex <= lastIndex - 1 && value[temp] < value[rightIndex])
                temp = rightIndex;

            if (temp != currIndex) {
                swap(value, temp, currIndex);
                currIndex = temp;
            } else
                break;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int[] DoMergeSort(int[] value) {
        int arrSize = value.length;

        if (arrSize > 1) {
            int[] leftArr = Arrays.copyOfRange(value, 0, arrSize / 2);
            int[] rightArr = Arrays.copyOfRange(value, arrSize / 2, arrSize);

            DoMergeSort(leftArr);
            DoMergeSort(rightArr);
            merge(value, leftArr, rightArr);
        }

        return (value);
    }

    private static void merge(int value[], int[] leftArr, int[] rightArr) {
        int leftArrSize = leftArr.length;
        int rightArrSize = rightArr.length;
        int leftIndex = 0;
        int rightIndex = 0;
        int valueIndex = 0;

        while (valueIndex < value.length) {
            if (leftIndex < leftArrSize && rightIndex < rightArrSize)
                value[valueIndex++] = (leftArr[leftIndex] < rightArr[rightIndex] ? leftArr[leftIndex++]
                        : rightArr[rightIndex++]);
            else {
                if (leftIndex >= leftArrSize)
                    value[valueIndex++] = rightArr[rightIndex++];
                else
                    value[valueIndex++] = leftArr[leftIndex++];
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int[] DoQuickSort(int[] value) {
        return quickSort(value, 0, value.length - 1);
    }

    private static int[] quickSort(int[] value, int left, int right) {
        int pivotIndex;

        if (right > left) {
            pivotIndex = partition(value, left, right);
            quickSort(value, left, pivotIndex - 1);
            quickSort(value, pivotIndex + 1, right);
        }

        return (value);
    }

    private static int partition(int[] value, int left, int right) {
        int pivot = value[left];
        int pivotIndex;
        int start = left, end = right + 1;

        while (start < end) {
            while (start < right && value[++start] < pivot)
                ;
            while (end > left && value[--end] > pivot)
                ;

            if (start < end)
                swap(value, start, end);
            else
                break;
        }

        pivotIndex = end;
        swap(value, end, left);

        return pivotIndex;
    }

    private static void swap(int[] value, int i, int j) {
        int temp;

        temp = value[j];
        value[j] = value[i];
        value[i] = temp;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private static int[] DoRadixSort(int[] value) {
        int[] negValue = new int[value.length];
        int[] posValue = new int[value.length];
        int negValueCount = 0, posValueCount = 0;

        for (int i = 0; i < value.length; i++) {
            // 음수에 대하여도 Radix Sort를 적용하기 위해, 음수를 다른 배열에 절댓값을 씌워서 저장한다.
            if (value[i] < 0)
                negValue[negValueCount++] = Math.abs(value[i]);
            else
                posValue[posValueCount++] = value[i];
        }

        if (negValueCount > 1)
            radixSort(negValue, negValueCount);
        if (posValueCount > 1)
            radixSort(posValue, posValueCount);

        // Radix Sort를 통해 정렬된 음수는 절댓값이 큰 순서대로 가져오고 -1을 곱하면 된다.
        // 이렇게 하면 음수의 Radix Sort가 완성된다.
        for (int i = negValueCount - 1; i >= 0; i--)
            value[negValueCount - 1 - i] = -1 * negValue[i];
        for (int i = 0; i < posValueCount; i++)
            value[negValueCount + i] = posValue[i];

        return (value);
    }

    private static void radixSort(int[] value, int size) {
        int maxItem = 0;

        for (int i = 0; i < size; i++) {
            if (maxItem < value[i])
                maxItem = value[i];
        }
        int maxLength = String.valueOf(maxItem).length();

        int[] sortedValue = new int[size];

        // 일의 자리에서부터, maxLength의 자리까지 수행한다.
        for (int i = 0; i < maxLength; i++) {
            int[] bucket = new int[10];

            for (int j = 0; j < size; j++) {
                // 각 자리의 자릿수를 구하는 식이다.
                int bucketIndex = value[j] / (int) Math.pow(10, i) % 10;
                bucket[bucketIndex]++;
            }

            for (int k = 1; k < 10; k++)
                bucket[k] += bucket[k - 1];

            for (int l = size - 1; l >= 0; l--) {
                int bucketIndex = value[l] / (int) Math.pow(10, i) % 10;
                sortedValue[bucket[bucketIndex] - 1] = value[l];
                bucket[bucketIndex]--;
            }

            for (int x = 0; x < size; x++)
                value[x] = sortedValue[x];
        }
    }
}