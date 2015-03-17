/*
 * Copyright 2015 Matthew Aguirre
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tros.logo;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ParserRuleContext;
import org.tros.logo.antlr.logoParser;
import org.tros.logo.antlr.logoParser.ProcedureInvocationContext;
import org.tros.torgo.CodeFunction;
import org.tros.torgo.InterpreterValue;
import org.tros.torgo.ReturnValue;
import org.tros.torgo.ReturnValue.ProcessResult;
import org.tros.torgo.Scope;
import org.tros.torgo.TorgoCanvas;

/**
 * Supports functions with parameters.
 *
 * @author matta
 */
class LogoFunction extends LogoBlock implements CodeFunction {

    private final String funcitonName;

    /**
     * Constructor
     *
     * @param functionName
     * @param ctx
     */
    protected LogoFunction(String functionName, ParserRuleContext ctx) {
        super(ctx);
        this.funcitonName = functionName;
    }

    /**
     * Get the function name
     *
     * @return
     */
    @Override
    public String getFunctionName() {
        return funcitonName;
    }

    /**
     * Process the function.
     *
     * @param scope
     * @param canvas
     * @return
     */
    @Override
    public ReturnValue process(Scope scope, TorgoCanvas canvas) {
        ProcedureInvocationContext context = (ProcedureInvocationContext) scope.peek().getParserRuleContext();

        logoParser.ProcedureDeclarationContext funct = (logoParser.ProcedureDeclarationContext) ctx;
        ArrayList<String> paramNames = new ArrayList<>();
        ArrayList<InterpreterValue> paramValues = new ArrayList<>();

        funct.parameterDeclarations().stream().forEach((param) -> {
            paramNames.add(param.getText().substring(1));
        });

        context.expression().stream().map((exp) -> {
            return ExpressionListener.evaluate(scope, exp);
        }).forEach((el) -> {
            paramValues.add(el);
        });

        if (paramNames.size() == paramValues.size()) {
            for (int ii = 0; ii < paramNames.size(); ii++) {
                scope.setNew(paramNames.get(ii), paramValues.get(ii));
            }
        }

        ReturnValue ret = null;
        try {
            Logger.getLogger(LogoFunction.class.getName()).log(Level.FINEST, "function: {0}", new Object[]{funcitonName});
            for (int ii = 0; ii < paramNames.size(); ii++) {
                String name = paramNames.get(ii);
                InterpreterValue value = paramValues.get(ii);
                Logger.getLogger(LogoFunction.class.getName()).log(Level.FINEST, "param: {0} -> {1}", new Object[]{name, value});
            }
            ret = super.process(scope, canvas);
        } catch (Exception ex) {
            Logger.getLogger(LogoFunction.class.getName()).log(Level.WARNING, "{0} -> {1}", new Object[]{ex.getClass().getName(), ex.getMessage()});
        }
        if (ret != null && ret.getResult() != ProcessResult.HALT) {
            //NOT HALT!
            ret = new ReturnValue(ret.getType(), ret.getValue(), ProcessResult.SUCCESS);
        } else {
            //HALT or ERROR (possible null)!
            ret = ReturnValue.HALT;
        }

        return ret;
    }
}