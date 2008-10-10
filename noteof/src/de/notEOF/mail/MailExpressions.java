package de.notEOF.mail;

import java.util.ArrayList;
import java.util.List;

import de.notEOF.mail.interfaces.MailMatchExpressions;

public class MailExpressions implements MailMatchExpressions {

    private List<String> expressions;

    public void setExpressions(List<String> expressions) {
        this.expressions = expressions;
    }

    public void addAll(List<String> expressions) {
        if (null == expressions) {
            expressions = new ArrayList<String>();
        }
        this.expressions.addAll(expressions);
    }

    public List<String> getExpressions() {
        return expressions;
    }

    public void add(String expression) {
        if (null == expressions) {
            expressions = new ArrayList<String>();
        }
        expressions.add(expression);
    }

}
