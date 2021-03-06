package com.alibaba.simpleEL.dialect.ql;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.simpleEL.Expr;
import com.alibaba.simpleEL.ExprCacheProvider;
import com.alibaba.simpleEL.JavaSource;
import com.alibaba.simpleEL.JavaSourceCompiler;
import com.alibaba.simpleEL.compile.JdkCompiler;
import com.alibaba.simpleEL.eval.DefaultExprCacheProvider;
import com.alibaba.simpleEL.eval.DefaultExpressEvalService;

public class QLEvalService extends DefaultExpressEvalService {

    public QLEvalService(){
        super(new QLPreprocessor());
    }

    public void setPreprocessor(QLPreprocessor preprocessor) {
        this.preprocessor = preprocessor;
    }

    public <T> void select(Class<T> clazz, Collection<T> srcCollection, Collection<T> destCollection, String ql,
                           Map<String, Object> context) throws Exception {
        Expr compiledExpr = getExpr(Collections.<String, Object> singletonMap("class", clazz), ql);

        Map<String, Object> evalContext = new HashMap<String, Object>();
        evalContext.put("_src_", srcCollection);
        evalContext.put("_dest_", destCollection);
        evalContext.putAll(context);

        compiledExpr.eval(evalContext);
    }

    public Expr getExpr(Map<String, Object> compileContext, String expr) throws InstantiationException,
                                                                        IllegalAccessException {
        Expr cachedExpr = null;

        if (cacheProvider != null) {
            cachedExpr = cacheProvider.get(compileContext, expr);
        }

        if (cachedExpr != null) {
            return cachedExpr;
        }

        JavaSource source = preprocessor.handle(compileContext, expr);

        System.out.println(source.getSource());

        Class<? extends Expr> exprClass = compiler.compile(source);

        Expr compiledExpr = exprClass.newInstance();

        cacheProvider.putIfAbsent(compileContext, expr, compiledExpr);

        cachedExpr = cacheProvider.get(compileContext, expr);

        return cachedExpr;
    }
}
