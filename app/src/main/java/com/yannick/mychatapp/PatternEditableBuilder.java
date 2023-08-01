package com.yannick.mychatapp;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternEditableBuilder {
    ArrayList<SpannablePatternItem> patterns;

    public class SpannablePatternItem {
        public SpannablePatternItem(Pattern pattern, SpannableStyleListener styles, SpannableClickedListener listener) {
            this.pattern = pattern;
            this.styles = styles;
            this.listener = listener;
        }

        public SpannableStyleListener styles;
        public Pattern pattern;
        public SpannableClickedListener listener;
    }

    public static abstract class SpannableStyleListener {
        public int spanTextColor;

        public SpannableStyleListener() {
        }

        public SpannableStyleListener(int spanTextColor) {
            this.spanTextColor = spanTextColor;
        }

        abstract void onSpanStyled(TextPaint ds);
    }

    public interface SpannableClickedListener {
        void onSpanClicked(String text);
    }

    public class StyledClickableSpan extends ClickableSpan {
        SpannablePatternItem item;

        public StyledClickableSpan(SpannablePatternItem item) {
            this.item = item;
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            if (item.styles != null) {
                item.styles.onSpanStyled(ds);
            }
            super.updateDrawState(ds);
        }

        @Override
        public void onClick(View widget) {
            if (item.listener != null) {
                TextView tv = (TextView) widget;
                Spanned span = (Spanned) tv.getText();
                int start = span.getSpanStart(this);
                int end = span.getSpanEnd(this);
                CharSequence text = span.subSequence(start, end);
                item.listener.onSpanClicked(text.toString());
            }
            widget.invalidate();
        }
    }

    public PatternEditableBuilder() {
        this.patterns = new ArrayList<>();
    }

    public PatternEditableBuilder addPattern(Pattern pattern, SpannableStyleListener spanStyles, SpannableClickedListener listener) {
        patterns.add(new SpannablePatternItem(pattern, spanStyles, listener));
        return this;
    }

    public PatternEditableBuilder addPattern(Pattern pattern, SpannableStyleListener spanStyles) {
        addPattern(pattern, spanStyles, null);
        return this;
    }

    public PatternEditableBuilder addPattern(Pattern pattern) {
        addPattern(pattern, null, null);
        return this;
    }

    public PatternEditableBuilder addPattern(Pattern pattern, int textColor) {
        addPattern(pattern, textColor, null);
        return this;
    }

    public PatternEditableBuilder addPattern(Pattern pattern, int textColor, SpannableClickedListener listener) {
        SpannableStyleListener styles = new SpannableStyleListener(textColor) {
            @Override
            public void onSpanStyled(TextPaint ds) {
                ds.linkColor = this.spanTextColor;
            }
        };
        addPattern(pattern, styles, listener);
        return this;
    }

    public PatternEditableBuilder addPattern(Pattern pattern, SpannableClickedListener listener) {
        addPattern(pattern, null, listener);
        return this;
    }

    public void into(TextView textView) {
        SpannableStringBuilder result = build(textView.getText());
        textView.setText(result);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public SpannableStringBuilder build(CharSequence editable) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(editable);
        for (SpannablePatternItem item : patterns) {
            Matcher matcher = item.pattern.matcher(ssb);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                StyledClickableSpan url = new StyledClickableSpan(item);
                ssb.setSpan(url, start, end, 0);
            }
        }
        return ssb;
    }
}