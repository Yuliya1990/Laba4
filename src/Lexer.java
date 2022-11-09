import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Lexer {

    private List<TokenDefenition> _tokenDefenitions;

    public Lexer()
    {
        _tokenDefenitions = new ArrayList<TokenDefenition>();
        FillAllTokenDefenitions();
    }

    public List<Token> Tokenize(String filePath)
    {
        List<Token> tokenizedProgram = new ArrayList<>();

        BufferedReader reader;
        try
        {
            reader = new BufferedReader(new FileReader(filePath));
            String codeLine = reader.readLine();
            while (codeLine != null)
            {
                Map<Integer, List<Token>> tokenTypesForLexemas = GetTokenTypesForLexemas(codeLine);
                int lastEndIndex = -1;
                for(var tokensForLexema : tokenTypesForLexemas.entrySet())
                {
                    Token rightToken = null;
                    for(var token : tokensForLexema.getValue())
                    {
                        if (lastEndIndex >= token.endIndex)
                            continue;
                        if(rightToken == null || token.endIndex > rightToken.endIndex
                                || (token.endIndex == rightToken.endIndex && token.tokenType.compareTo(rightToken.tokenType) < 0))
                        {
                            rightToken = token;
                        }
                    }
                    if (rightToken != null)
                    {
                        lastEndIndex = rightToken.endIndex;
                        tokenizedProgram.add(rightToken);
                    }
                }
                codeLine = reader.readLine();
            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return tokenizedProgram;
    }
    private Map<Integer,List<Token>> GetTokenTypesForLexemas(String line)
    {
        ArrayList<Token> lineMatches = new ArrayList<>();
        for (TokenDefenition tokenDefinition : _tokenDefenitions)
        {
            lineMatches.addAll(tokenDefinition.FindMatches(line));
        }
        Map<Integer,List<Token>> tokensByStartIndex = new TreeMap<Integer,List<Token>>();
        tokensByStartIndex.putAll(lineMatches.stream().collect(Collectors.groupingBy(Token:: GetStartIndex)));
        return  tokensByStartIndex;
    }

    private void FillAllTokenDefenitions()
    {
        _tokenDefenitions.add(new TokenDefenition(new String("#nullable|#if|#elif|#else|#endif|#define|#undef|#region|#endregion|#error|#warning|#line|#pragma|#pragma warning|#pragma checksum"), TokenType.CompilerDirective));

            _tokenDefenitions.add(new TokenDefenition("^[/\\*](.*)[\\*\\/]$", TokenType.Comment));

            _tokenDefenitions.add(new TokenDefenition("int|double|foreach|abstract|base|bool|break|byte|case|catch|char|checked|class|const|continue|decimal|delegate|do|double|else|enum|event|explicit|extern|finally|fixed|float|for|foreach|goto|if|implicit|in|int|interface|internal|lock|long|namespace|new|object|operator|out|override|params|private|protected|public|readonly|ref|return|sbyte|sealed|short|sizeof|static|string|struct|switch|this|throw|try|typeof|uint|ulong|unchecked|unsafe|ushort|using|virtual|void|var|volatile|while", TokenType.ReservedWord));

            _tokenDefenitions.add(new TokenDefenition("(\\+=|<<|>>|>=|<=|is|as|==|!=|&&|\\|\\||\\+=|-=|\\*=|/=|%=|^=|<<=|>>=|\\?\\?=|=>|\\+\\+|--|[+]|[-]|[\\*]|/|=|<|>|&|%)", TokenType.Operator));

            _tokenDefenitions.add(new TokenDefenition("\\b\\d+|\\b\\d+.\\d+|\\b\\d+e\\d+|0[xX][0-9a-fA-F]+", TokenType.Number));

            _tokenDefenitions.add(new TokenDefenition("'(.*)'|\"(.*)\"", TokenType.StringOrCharacter));

            _tokenDefenitions.add(new TokenDefenition("([()\\[\\]{},:;.])", TokenType.Separator));

            _tokenDefenitions.add(new TokenDefenition("([a-z]|[A-Z]|\\d|[_])+", TokenType.Identificator));

            _tokenDefenitions.add(new TokenDefenition("(`)+", TokenType.Invalid));
}
}
