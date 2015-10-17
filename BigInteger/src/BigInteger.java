import java.io.*;

public class BigInteger {
    public static void main(String args[]) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                String input = br.readLine();
                if (input.compareTo("quit") == 0) {
                    break;
                }

                calculate(input);
            } catch (Exception e) {
                System.out.println("입력이 잘못되었습니다. 오류 : " + e.toString());
            }
        }
    }

    private static char[] num1;
    private static char[] num2;
    private static char[] result;

    // 계산의 편의를 위해서 num1과 num2의 숫자를 뒤집어 놓을 때 저장해두는 배열이다.
    private static char[] reverseNum1;
    private static char[] reverseNum2;
    private static char[] reverseResult;

    private static char midOperator;
    private static char firstOperator;
    private static char secondOperator;
    private static char resultOperator;

    private static boolean zeroIsResult;
    private static int highestCarryForMul;

    private static void calculate(String input) {
        input = input.trim();
        zeroIsResult = false; // 입력을 받아서 연산을 시행할 때마다 오류가 일어나지 않도록 우선 0이 result 값이 아니라고 초기화한다.
        highestCarryForMul = 0;

        char tempOperator; // 덧셈과 뺄셈에서 부호 처리를 하기 위한 중간 과정에서 쓰이는 변수이다.
        int cmpLength; // num1과 num2 중 어떤 것이 길이 우위를 나타내는지 저장해두는 변수이다.
        int temp = findMidOperatorIndex(input); // num1과 num2 사이에 있는 중간 연산자의 Index를 찾는다.

        // 중간 연산자 앞 뒤로 있는 문자들을 tempNum1과 tempNum2에 우선 담는다.
        String tempNum1 = input.substring(0, temp).trim();
        String tempNum2 = input.substring(temp + 1).trim();

        int lengthNum1, lengthNum2;

        // tempNum1, 2에서 부호(+, -)를 제외한 수만을 다시 tempNum1, 2에 담는다.
        if (isOperator(tempNum1.charAt(0)) == true) {
            firstOperator = tempNum1.charAt(0);
            tempNum1 = tempNum1.substring(1).trim();
        } else
            firstOperator = '+';

        if (isOperator(tempNum2.charAt(0)) == true) {
            secondOperator = tempNum2.charAt(0);
            tempNum2 = tempNum2.substring(1).trim();
        } else
            secondOperator = '+';

        // 부호를 제외한 숫자만의 길이를 lengthNum1, 2에 각각 저장한다.
        lengthNum1 = tempNum1.length();
        lengthNum2 = tempNum2.length();

        // 두 숫자의 길이를 비교하여 cmpLength에 길이 우위를 저장한다.
        if (lengthNum1 > lengthNum2) {
            num1 = new char[lengthNum1];
            num2 = new char[lengthNum1];
            cmpLength = 1; // lengthNum1 > lengthNum2
        } else if (lengthNum1 < lengthNum2) {
            num1 = new char[lengthNum2];
            num2 = new char[lengthNum2];
            cmpLength = 2; // lengthNum1 < lengthNum2
        } else {
            num1 = new char[lengthNum1];
            num2 = new char[lengthNum1];
            cmpLength = 3; // lengthNum1 == lengthNum2
        }

        // char 배열에 두 숫자의 각 자릿수를 모두 저장한다.
        for (int i = 0; i < lengthNum1; i++)
            num1[i] = tempNum1.charAt(i);
        for (int i = 0; i < lengthNum2; i++)
            num2[i] = tempNum2.charAt(i);

        // 본격적으로 곱셈, 덧셈, 뺄셈 연산이 시작되는 부분이다.
        if (midOperator == '*') {
            // 아까 저장한 중간 연산자를 보고 곱셈인지 아닌지 먼저 구분한다.
            if (firstOperator == secondOperator)
                resultOperator = '+';
            else
                resultOperator = '-';

            if (num1[0] == '0' || num2[0] == '0')
                mulResultZero();
            else
                mulNum(lengthNum1, lengthNum2);
        } else {
            // 중간 연산자가 덧셈 or 뺄셈이라면 생각한 부호 처리 알고리즘에 따라서 연산을 시행한다.
            if (midOperator == secondOperator)
                tempOperator = '+';
            else
                tempOperator = '-';

            if (tempOperator == firstOperator) {
                addNum(cmpLength, lengthNum1, lengthNum2);
                resultOperator = firstOperator;
            } else {
                secondOperator = tempOperator;
                subNum(cmpLength, lengthNum1, lengthNum2);
            }
        }

        // 최종 결과를 출력한다.
        printingResult();
    }

    // 중간 연산자의 Index를 찾는 함수의 내용 부분이다.
    private static int findMidOperatorIndex(String input) {
        int i;

        for (i = 1; i < input.length(); i++) {
            if (isOperator(input.charAt(i)) == true) {
                midOperator = input.charAt(i);
                break;
            }
        }
        return i;
    }

    // 매개변수 ch가 연산자인지 확인하는 함수이다. +, -, *인지 확인하는 기능을 제공한다.
    private static boolean isOperator(char ch) {
        boolean flag = false;

        switch (ch) {
        case '+':
            flag = true;
            break;
        case '-':
            flag = true;
            break;
        case '*':
            flag = true;
            break;
        }
        return flag;
    }

    // 덧셈을 연산하는 함수이다. 매개변수로는 num1과 num2 중 어떤 숫자가 더 긴지 저장한 변수와 lengthNum1,
    // lengthNum2를 받는다.
    private static void addNum(int cmpLength, int lengthNum1, int lengthNum2) {
        // num1과 num2의 숫자를 뒤집고, 후에 쉽게 replace 하기 위해 StringBuffer형 변수를 하나씩 생성하였다.
        StringBuffer tempStrBuf1 = new StringBuffer(new String(num1)).reverse();
        StringBuffer tempStrBuf2 = new StringBuffer(new String(num2)).reverse();

        String realPart;
        // 덧셈을 하기 위해서 num1과 num2 중 길이가 짧은 수에 0을 채워서 두 수의 길이를 맞추게 되는데,
        // 이 때 원래 길이가 짧았던 수에서 길이를 맞추기 위해 채워놓은 0을 제외한 realPart를 이 String 변수에 저장한다.

        if (cmpLength == 1) {
            realPart = tempStrBuf2.substring(lengthNum1 - lengthNum2);
            tempStrBuf2.replace(0, lengthNum2, realPart);

            for (int i = lengthNum2; i < lengthNum1; i++)
                tempStrBuf2.setCharAt(i, '0');
        } else if (cmpLength == 2) {
            realPart = tempStrBuf1.substring(lengthNum2 - lengthNum1);
            tempStrBuf1.replace(0, lengthNum1, realPart);

            for (int i = lengthNum1; i < lengthNum2; i++)
                tempStrBuf1.setCharAt(i, '0');
        }

        int finalLength = tempStrBuf1.length();
        reverseNum1 = new char[tempStrBuf1.length()];
        reverseNum2 = new char[tempStrBuf2.length()];
        reverseResult = new char[finalLength + 1];

        int[] addCarryArr = new int[finalLength];

        // 덧셈을 위해 자리까지 맞춰져 정리된 숫자들이 reverseNum1, 2 배열에 각각 담긴다.
        for (int i = 0; i < tempStrBuf1.length(); i++)
            reverseNum1[i] = tempStrBuf1.charAt(i);
        for (int i = 0; i < tempStrBuf2.length(); i++)
            reverseNum2[i] = tempStrBuf2.charAt(i);

        for (int i = 0; i < finalLength; i++) {
            addCarryArr[i] = addCarry(i - 1, addCarryArr);
            int x = Integer.parseInt(String.valueOf(reverseNum1[i])) + Integer.parseInt(String.valueOf(reverseNum2[i]));

            if (x + addCarryArr[i] >= 10) {
                if (i == tempStrBuf1.length() - 1)
                    reverseResult[i + 1] = '1';
                reverseResult[i] = (char) ((x + addCarryArr[i] - 10) + 48);
            } else {
                reverseResult[i] = (char) (x + addCarryArr[i] + 48);
            }
        }

        if (finalLength == 1 && reverseResult[0] == '0')
            zeroIsResult = true;

        orderingResult();
    }

    // 각 자리 숫자의 덧셈 시 받아올림이 생기는 부분을 처리해주는 함수이다.
    private static int addCarry(int prevNumIndex, int[] addCarryArr) {
        int carryNum;

        if (prevNumIndex == -1)
            carryNum = 0;
        else {
            int x = Integer.parseInt(String.valueOf(reverseNum1[prevNumIndex]));
            int y = Integer.parseInt(String.valueOf(reverseNum2[prevNumIndex]));

            if (x + y + addCarryArr[prevNumIndex] >= 10)
                carryNum = 1;
            else
                carryNum = 0;
        }

        return carryNum;
    }

    // 뺄셈을 연산하는 함수에 대한 내용이다. 매개변수는 덧셈과 같다.
    private static void subNum(int cmpLength, int lengthNum1, int lengthNum2) {
        // num1과 num2의 숫자를 뒤집고, 후에 쉽게 replace 하기 위해 StringBuffer형 변수를 하나씩 생성하였다.
        StringBuffer tempStrBuf1 = new StringBuffer(new String(num1)).reverse();
        StringBuffer tempStrBuf2 = new StringBuffer(new String(num2)).reverse();

        String realPart;
        // 뺄셈을 하기 위해서 num1과 num2 중 길이가 짧은 수에 0을 채워서 두 수의 길이를 맞추게 되는데,
        // 이 때 원래 길이가 짧았던 수에서 길이를 맞추기 위해 채워놓은 0을 제외한 realPart를 이 String 변수에 저장한다

        // aIsBig 변수가 true이면 num1 숫자가 num2 숫자보다 큰 것이고, false이면 반대이다.
        boolean aIsBig = false;
        // bothIsEqual 변수가 true이면 num1 숫자와 num2 숫자가 같다.
        boolean bothIsEqual = false;

        if (cmpLength == 1) {
            aIsBig = true;
            realPart = tempStrBuf2.substring(lengthNum1 - lengthNum2);
            tempStrBuf2.replace(0, lengthNum2, realPart);

            for (int i = lengthNum2; i < lengthNum1; i++)
                tempStrBuf2.setCharAt(i, '0');
        } else if (cmpLength == 2) {
            aIsBig = false;
            realPart = tempStrBuf1.substring(lengthNum2 - lengthNum1);
            tempStrBuf1.replace(0, lengthNum1, realPart);

            for (int i = lengthNum1; i < lengthNum2; i++)
                tempStrBuf1.setCharAt(i, '0');
        } else if (cmpLength == 3) {
            for (int i = 0; i < num1.length; i++) {
                int x = Integer.parseInt(String.valueOf(num1[i]));
                int y = Integer.parseInt(String.valueOf(num2[i]));

                if (x > y) {
                    aIsBig = true;
                    break;
                } else if (x < y) {
                    aIsBig = false;
                    break;
                } else {
                    if (i == num1.length - 1) {
                        bothIsEqual = true;
                        break;
                    } else {
                        continue;
                    }
                }
            }
        }

        int finalLength = tempStrBuf1.length();
        reverseNum1 = new char[tempStrBuf1.length()];
        reverseNum2 = new char[tempStrBuf2.length()];
        reverseResult = new char[finalLength];

        int[] subBorrowArr = new int[finalLength];

        // 뺄셈을 위해 자리까지 맞춰져 정리된 숫자들이 reverseNum1, 2 배열에 각각 담긴다.
        for (int i = 0; i < tempStrBuf1.length(); i++)
            reverseNum1[i] = tempStrBuf1.charAt(i);
        for (int i = 0; i < tempStrBuf2.length(); i++)
            reverseNum2[i] = tempStrBuf2.charAt(i);

        // 먼저 num1과 num2이 같은지 본다. 같으면, reverseResult 배열에 0을 넣는다.
        if (bothIsEqual == true) {
            reverseResult[0] = '0';
            zeroIsResult = true;
        } else {
            // num1과 num2가 같지 않으면, aIsBig 변수에 따라서 큰 수에서 작은 수를 빼는 방식으로 뺄셈이 진행되게 된다.
            if (aIsBig == true) {
                for (int i = 0; i < finalLength; i++) {
                    int x = Integer.parseInt(String.valueOf(reverseNum1[i]));
                    int y = Integer.parseInt(String.valueOf(reverseNum2[i]));

                    if (x - subBorrowArr[i] < y) {
                        subBorrowArr[i + 1] = subBorrow(i, aIsBig, subBorrowArr);
                        reverseResult[i] = (char) (10 - y + (x - subBorrowArr[i]) + 48);
                    } else {
                        reverseResult[i] = (char) ((x - subBorrowArr[i]) - y + 48);
                        // 48은 ASCII 코드와 관련이 있다.
                    }
                }
                resultOperator = firstOperator;
            } else {
                for (int i = 0; i < finalLength; i++) {
                    int x = Integer.parseInt(String.valueOf(reverseNum2[i]));
                    int y = Integer.parseInt(String.valueOf(reverseNum1[i]));

                    if (x - subBorrowArr[i] < y) {
                        subBorrowArr[i + 1] = subBorrow(i, aIsBig, subBorrowArr);
                        reverseResult[i] = (char) (10 - y + (x - subBorrowArr[i]) + 48);
                    } else {
                        reverseResult[i] = (char) ((x - subBorrowArr[i]) - y + 48);
                        // 48은 ASCII 코드와 관련이 있다.
                    }
                }
                resultOperator = secondOperator;
            }

            for (int i = 0; i < finalLength; i++) {
                if (reverseResult[finalLength - 1 - i] == '0')
                    reverseResult[finalLength - 1 - i] = ' ';
                else {
                    break;
                }
            }
        }

        orderingResult();
    }

    // 뺄셈 시 받아내림과 관련된 함수의 내용이다.
    private static int subBorrow(int prevNumIndex, boolean aIsBig, int[] subBorrowArr) {
        int borrowNum;

        int x = Integer.parseInt(String.valueOf(reverseNum1[prevNumIndex]));
        int y = Integer.parseInt(String.valueOf(reverseNum2[prevNumIndex]));

        if (x - subBorrowArr[prevNumIndex] < y && aIsBig == true)
            borrowNum = 1;
        else if (x > y - subBorrowArr[prevNumIndex] && aIsBig == false)
            borrowNum = 1;
        else
            borrowNum = 0;

        return borrowNum;
    }

    // 덧셈과 뺄셈에서 reverse된 결과값을 올바른 순서대로 result에 담는 함수이다.
    private static void orderingResult() {
        String trimmedReverseResult = new String(reverseResult).trim();
        int length = trimmedReverseResult.length();

        result = new char[length];
        for (int i = 0; i < length; i++)
            result[i] = trimmedReverseResult.charAt(length - 1 - i);
    }

    // 곱셈과 관련된 함수의 내용이다.
    private static void mulNum(int lengthNum1, int lengthNum2) {
        // 입력 시 받은 num1과 num2의 숫자들을 계산의 편의를 위해 뒤집고, 앞 뒤 공백을 제거한다.
        StringBuffer tmpStrBuf1 = new StringBuffer(new String(num1)).reverse();
        StringBuffer tmpStrBuf2 = new StringBuffer(new String(num2)).reverse();
        tmpStrBuf1 = new StringBuffer(new String(tmpStrBuf1).trim());
        tmpStrBuf2 = new StringBuffer(new String(tmpStrBuf2).trim());

        reverseNum1 = new char[lengthNum1];
        reverseNum2 = new char[lengthNum2];

        // 쉽게 곱셈을 하기 위해 정리된 뒤집어진 수를 reverseNum 배열에 한 글자씩 넣는다.
        for (int i = 0; i < lengthNum1; i++)
            reverseNum1[i] = tmpStrBuf1.charAt(i);
        for (int i = 0; i < lengthNum2; i++)
            reverseNum2[i] = tmpStrBuf2.charAt(i);

        // 곱셈은 덧셈의 연속으로 코드를 작성했다.
        // getLengthForAdding() 함수는 덧셈 시, 앞 공백에 0을 채워놓는 등 덧셈을 위해 맞춰줘야되는 자릿수를 리턴한다.
        int allLength = getLengthForAdding(lengthNum1, lengthNum2);

        int[][] midArray = new int[lengthNum2][allLength];
        int[] midResult = new int[allLength];
        int[] mulCarryArr = new int[lengthNum1];

        for (int i = 0; i < lengthNum2; i++) {
            int k;

            // mulCarryArr 초기화
            for (int x = 0; x < lengthNum1; x++)
                mulCarryArr[x] = 0;

            for (k = 0; k < i; k++)
                midArray[i][k] = 0;

            for (int j = 0; j < lengthNum1; j++) {
                int x = Integer.parseInt(String.valueOf(reverseNum1[j]));
                int y = Integer.parseInt(String.valueOf(reverseNum2[i]));

                if (j == (lengthNum1 - 1)) {
                    if ((x * y + mulCarryArr[j]) >= 10) {
                        midArray[i][j + k + 1] = (x * y + mulCarryArr[j]) / 10;
                        midArray[i][j + k] = (x * y + mulCarryArr[j]) % 10;
                    } else {
                        midArray[i][j + k] = (x * y + mulCarryArr[j]) % 10;
                    }
                } else {
                    if ((x * y + mulCarryArr[j]) >= 10) {
                        mulCarryArr[j + 1] = (x * y + mulCarry(j - 1, i, mulCarryArr)) / 10;
                        midArray[i][j + k] = (x * y + mulCarryArr[j]) % 10;
                    } else {
                        midArray[i][j + k] = (x * y + mulCarryArr[j]) % 10;
                    }
                }
            }
        }

        if (lengthNum2 == 1) {
            for (int i = 0; i < allLength; i++)
                midResult[i] = midArray[0][i];
        } else if (lengthNum2 == 2) {
            for (int i = 0; i < allLength; i++)
                System.arraycopy(addForMul(midArray[0], midArray[1], allLength), 0, midResult, 0, allLength);
        } else {
            for (int i = 0; i < allLength; i++)
                midResult[i] = midArray[lengthNum2 - 1][i];

            for (int i = lengthNum2 - 1; i >= 1; i--)
                System.arraycopy(addForMul(midResult, midArray[i - 1], allLength), 0, midResult, 0, allLength);
        }

        orderingResultForMul(midResult);
    }

    // 곱셈에서 midResult 배열에 담긴 reverse된 결과값을 result 배열에 올바른 순서대로 담을 때 사용하는 함수이다.
    private static void orderingResultForMul(int[] prevReverseArr) {
        int length = prevReverseArr.length;

        if (prevReverseArr[length - 1] == 0 || highestCarryForMul == 1) {
            result = new char[length + 1];
            result[0] = '1';

            for (int i = 0; i < length; i++)
                result[i + 1] = (char) (prevReverseArr[length - 1 - i] + 48);
        } else {
            result = new char[length];

            for (int i = 0; i < length; i++)
                result[i] = (char) (prevReverseArr[length - 1 - i] + 48);
        }
    }

    // 곱셈은 덧셈의 연속으로 코드를 작성했다. 덧셈을 하기 위해서는 자리를 맞춰야 하는데, 그 때 덧셈을 하기 위한 자릿수를 리턴한다.
    private static int getLengthForAdding(int lengthNum1, int lengthNum2) {
        int allLength = 0;
        int[] mulCarry = new int[lengthNum1 + 1];

        for (int i = 0; i < lengthNum1; i++) {
            int x = Integer.parseInt(String.valueOf(reverseNum1[i]));
            int y = Integer.parseInt(String.valueOf(reverseNum2[lengthNum2 - 1]));

            if (x * y + mulCarry[i] >= 10)
                mulCarry[i + 1] = (x * y + mulCarry[i]) / 10;

            if (i == (lengthNum1 - 1)) {
                if ((x * y + mulCarry[i]) >= 10)
                    allLength += 2;
                else
                    allLength += 1;
            } else {
                allLength += 1;
            }
        }
        allLength += (lengthNum2 - 1);

        return allLength;
    }

    // 이전에 더해진 Carry가 새로운 Carry 발생에 영향을 줄 때 사용하는 Carry 리턴 함수이다.
    private static int mulCarry(int firstNumPrevIndex, int secondNumIndex, int[] mulCarryArr) {
        int carryNum = 0;

        if (firstNumPrevIndex == -1)
            carryNum = 0;
        else {
            int x = Integer.parseInt(String.valueOf(reverseNum1[firstNumPrevIndex]));
            int y = Integer.parseInt(String.valueOf(reverseNum2[secondNumIndex]));

            carryNum = (x * y + mulCarryArr[firstNumPrevIndex]) / 10;
        }

        return carryNum;
    }

    private static int[] addForMul(int[] num1, int[] num2, int allLength) {
        int[] midResult = new int[allLength];
        int carry = 0;

        for (int i = 0; i < allLength; i++) {
            if (num1[i] + num2[i] + carry >= 10) {
                midResult[i] = (num1[i] + num2[i] + carry) - 10;
                carry = 1;
                if (i == allLength - 1)
                    highestCarryForMul = 1;
            } else {
                midResult[i] = num1[i] + num2[i] + carry;
                carry = 0;
            }
        }
        return midResult;
    }

    // 곱셈에서 0이 곱해지면 무조건 0으로 처리하는 함수이다.
    private static void mulResultZero() {
        zeroIsResult = true;
        result = new char[1];
        result[0] = '0';
    }

    // 연산 최종 결과를 출력하는 함수이다.
    public static void printingResult() {
        int length = result.length;

        if (resultOperator == '-' && zeroIsResult != true)
            System.out.print(resultOperator);

        for (int i = 0; i < length; i++)
            System.out.print(result[i]);
        System.out.println();
    }
}