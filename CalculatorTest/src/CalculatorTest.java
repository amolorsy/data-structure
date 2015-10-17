import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.lang.Math;

public class CalculatorTest {
    public static void main(String args[]) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                String input = br.readLine();
                if (input.compareTo("q") == 0)
                    break;

                command(input);
            } catch (Exception e) {
                // postfix의 연산 시 연산이 불가능하여 발생하는 Error와 일부 input 자체에서 알 수 있는 Error에 대해서는 아래 코드에서
                // 처리한다.
                // 아래 코드에서 처리할 수 있는 Error는 피연산자가 없는 연산자의 사용, ")5+3("와 같은 잘못된 괄호의 사용,
                // Unary +의 사용, ***, //, */ 등 Binary 연산자의 중복 사용 등 다양하다.
                // 그 외에 Input 자체에서 미리 쉽게 발견할 수 있는 Error는 InfixToPostfix 클래스의 isInputError 메서드에서
                // 처리하였다.
                System.out.println("ERROR");
            }
        }
    }

    private static void command(String input) {
        int index = 0;
        char item;

        InfixToPostfix conversion = new InfixToPostfix();
        PostfixCalculator calculator = new PostfixCalculator();

        // postfix로 변환되기 전, input만으로도 미리 쉽게 Error가 발견되는 사항들에 대한 에러 처리다.
        if (conversion.isInputError(input.replaceAll("\\p{Space}+", " ").trim()))
            System.out.println("ERROR");
        else {
            input = input.replaceAll("\\p{Space}", "");
            // postfix로 쉽게 바꾸기 위해서 일단 Input의 공백을 모두 제거한다.

            while (index < input.length()) {
                // 공백이 제거된 Input을 한 글자씩 읽어온다.
                item = input.charAt(index);

                if (InfixToPostfix.isNum(item)) {
                    conversion.insertCharacter(item);
                } else if (item == '(') {
                    // 읽어들인 글자가 '('일 경우 수행.
                    conversion.push(item);
                } else if (item == ')') {
                    // 읽어들인 글자가 ')'일 경우 수행.
                    while (conversion.peek() != '(') {
                        // 스택의 최상위 element가 '('이 될 때까지 pop한다.
                        conversion.insertCharacter(' ');
                        conversion.insertCharacter(conversion.peek());
                        conversion.pop();
                    }
                    conversion.pop();
                } else {
                    conversion.insertCharacter(' ');
                    // postfix로 변환했을 때, 연산자와 피연산자 간의 공백을 만들어주는 것이다.

                    if (item == '-') // postfix 수식에서 unary를 쉽게 계산하기 위해, 읽어들인 글자가 unary '-'이면 '~'로 바꾼다.
                        item = conversion.binaryToUnaryMinus(input, item, index);

                    if (conversion.getAssociativity(item) == 0) { // left-associative 연산자를 처리하는 영역이다.
                        while (!(conversion.isEmpty())
                                && (conversion.getPriority(item) <= conversion.getPriority(conversion.peek()))) {
                            conversion.insertCharacter(conversion.peek());
                            conversion.insertCharacter(' ');
                            conversion.pop();
                        }
                    } else { // right-associative 연산자를 처리하는 영역이다.
                        while (!(conversion.isEmpty())
                                && (conversion.getPriority(item) < conversion.getPriority(conversion.peek()))) {
                            conversion.insertCharacter(conversion.peek());
                            conversion.insertCharacter(' ');
                            conversion.pop();
                        }
                    }
                    conversion.push(item);
                }
                index++;
            }
            conversion.allPop();
            // 여기까지가 InfixToPostfix Conversion에 관한 Code이다.

            // 여기부터는 Postfix로 변환된 수식의 연산에 관한 Code이다.
            String tokens = conversion.getPostfix();
            StringTokenizer tokenizer = new StringTokenizer(tokens, " ");
            // Postfix로 변환된 식을 공백을 기준으로 나누어 놓았다.

            while (tokenizer.hasMoreTokens()) {
                String temp = tokenizer.nextToken();
                if (InfixToPostfix.isNum(temp.charAt(0)))
                    calculator.push(temp);
                else
                    calculator.calculate(temp.charAt(0));
            }

            // postfix로 변환된 수식과 postfix 수식을 연산한 결과를 함께 출력한다.
            if (calculator.isError() != true)
                conversion.printResult();
            calculator.printResult();
        }
    }
}

// infix 수식을 postfix 수식으로 변환하는 것과 관련된 클래스이다.
class InfixToPostfix {
    private ArrayList<Character> stack;
    private StringBuffer postfix;

    InfixToPostfix() {
        stack = new ArrayList<Character>();
        postfix = new StringBuffer();
    }

    public void push(char item) {
        stack.add(Character.valueOf(item));
    }

    public void pop() {
        stack.remove(stack.size() - 1);
    }

    public void allPop() {
        while (isEmpty() != true) {
            insertCharacter(' ');
            insertCharacter(peek());
            pop();
        }
    }

    public char peek() {
        return stack.get(stack.size() - 1);
    }

    public boolean isInputError(String input) {
        // 초기에 Input을 확인하는 것만으로 쉽게 발견해낼 수 있는 Input ERROR를 이 메서드에서 잡아낸다.
        boolean isExist = false;
        int index = 0;
        int isParenthesisPair = 0;
        char item;

        // 빈 문자열 Input -> ERROR
        if (input.length() == 0)
            isExist = true;
        while (index < input.length()) {
            // isInputError 메서드에서 매개변수로 받은 Input은 공백을 포함하며, 그 공백은 최대 한 칸만 될 수 있다.
            item = input.charAt(index);

            if ((index != 0 && index != input.length() - 1) && item == ' ') {
                // '4 5'와 같이 숫자와 숫자 사이에 공백이 들어오는 Input -> ERROR
                if (isNum(input.charAt(index - 1)) && isNum(input.charAt(index + 1)))
                    isExist = true;
                // '( )'와 같이 괄호 사이에 공백만 들어오는 Input -> ERROR
                if (input.charAt(index - 1) == '(' && input.charAt(index + 1) == ')')
                    isExist = true;
                // ') ('와 같이 괄호를 잘못 사용하는 경우 -> ERROR
                if (input.charAt(index - 1) == ')' && input.charAt(index + 1) == '(')
                    isExist = true;
                // '4 ('와 같이 숫자 + 공백 한 칸 + 여는 괄호 -> ERROR
                if (isNum(input.charAt(index - 1)) && input.charAt(index + 1) == '(')
                    isExist = true;
                // ') 5'와 같이 닫는 괄호 + 공백 한 칸 + 숫자 -> ERROR
                if (input.charAt(index - 1) == ')' && isNum(input.charAt(index + 1)))
                    isExist = true;
            } else if (item == '(') {
                // '4('와 같이 숫자 다음에 공백, 연산자 없이 바로 여는 괄호가 오는 경우 -> ERROR
                if ((index != 0 && index != (input.length() - 1)) && isNum(input.charAt(index - 1)))
                    isExist = true;
                // '()'와 같이 괄호 사이에 어느 공백도 없는 경우 -> ERROR
                if (index != (input.length() - 1) && input.charAt(index + 1) == ')')
                    isExist = true;
                ++isParenthesisPair;
            } else if (item == ')') {
                // ')5'와 같이 닫는 괄호 다음에 연산자, 공백 없이 바로 숫자가 오는 경우 -> ERROR
                if ((index != 0 && index != (input.length() - 1)) && isNum(input.charAt(index + 1)))
                    isExist = true;
                // ')('처럼 닫는 괄호 뒤 연산자, 공백 없이 바로 여는 괄호가 오는 경우 -> ERROR
                if ((index != input.length() - 1) && input.charAt(index + 1) == '(')
                    isExist = true;
                --isParenthesisPair;
            } else {
                // 지원하지 않는 연산자가 포함된 경우 -> ERROR
                if (!isNum(item) && getPriority(item) == 0)
                    isExist = true;
                // postfix로 변환된 식에만 있어야 되는 '~'가 포함된 경우 -> ERROR
                else if (!isNum(item) && getPriority(item) == 3)
                    isExist = true;
            }
            index++;
        }

        // Input 전체에 괄호 짝이 맞지 않는 경우 -> ERROR
        if (isParenthesisPair != 0)
            isExist = true;

        return isExist;
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public static boolean isNum(char ch) {
        boolean flag = false;

        for (int i = '0'; i <= '9'; i++) {
            if (ch == i) {
                flag = true;
                break;
            }
        }

        return flag;
    }

    public void insertCharacter(char item) {
        postfix.append(item);
    }

    public char binaryToUnaryMinus(String input, char item, int index) {
        // infix 수식의 unary '-'를 '~'로 바꿔서 리턴하는 함수이다.
        if (index == 0)
            item = '~';
        else {
            if (!isNum(input.charAt(index - 1)) && input.charAt(index - 1) != ')')
                item = '~';
        }

        return item;
    }

    public int getPriority(char item) {
        int priority;

        if (item == '^')
            priority = 4;
        else if (item == '~')
            priority = 3;
        else if (item == '*' || item == '/' || item == '%')
            priority = 2;
        else if (item == '+' || item == '-')
            priority = 1;
        else
            priority = 0; // 인자로 건네받은 item이 priority가 0이면 지원하지 않는 연산자라는 것이다.

        return priority;
    }

    public int getAssociativity(char item) {
        int associativity;

        if (item != '^' && item != '~')
            associativity = 0; // associativity == 0 -> left-associative
        else
            associativity = 1; // associativity == 1 -> right-associative

        return associativity;
    }

    public String getPostfix() {
        return (new String(postfix).replaceAll("\\p{Space}+", " ").trim());
    }

    public void printResult() {
        System.out.println(getPostfix());
    }
}

class PostfixCalculator {
    private ArrayList<Long> stack;
    private boolean needToPrintError = false;

    PostfixCalculator() {
        stack = new ArrayList<Long>();
    }

    public void push(Object item) {
        if (item.getClass().getSimpleName().equals("String")) {
            if (InfixToPostfix.isNum(((String) item).charAt(0)))
                stack.add(Long.parseLong((String) item));
        } else
            stack.add((long) item);
    }

    public long pop() {
        long temp = stack.get(stack.size() - 1);
        stack.remove(stack.size() - 1);

        return temp;
    }

    public void calculate(char operator) {
        if (operator == '~') {
            long op = pop();
            push(op * -1);
        } else {
            long op2 = pop();
            long op1 = pop();

            if (operator == '+')
                push(op1 + op2);
            else if (operator == '-')
                push(op1 - op2);
            else if (operator == '*')
                push(op1 * op2);
            else if (operator == '/')
                push(op1 / op2);
            else if (operator == '%')
                push(op1 % op2);
            else if (operator == '^') {
                try {
                    // 0^(-2)와 같이 연산이 불가능한 경우에 대한 예외 처리이다.
                    if (op1 == 0 && op2 < 0) {
                        needToPrintError = true;
                        throw new Exception();
                    } else
                        push((long) Math.pow((double) op1, (double) op2));
                } catch (Exception e) {
                }
            }
        }
    }

    public boolean isError() {
        boolean isError = false;

        if (needToPrintError == true)
            isError = true;

        return isError;
    }

    public void printResult() {
        System.out.println(pop());
    }
}