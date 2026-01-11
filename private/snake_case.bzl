"""Utilities for string case conversion."""

def camel_case_to_snake_case(camel_str):
    """Convert a camelCase string to snake_case.

    Args:
        camel_str: A camelCase string to convert.

    Returns:
        The snake_case version of the input string.
    """
    result = []
    for i in range(len(camel_str)):
        char = camel_str[i]
        if char.isupper():
            if i > 0:
                result.append("_")
            result.append(char.lower())
        else:
            result.append(char)
    return "".join(result)
