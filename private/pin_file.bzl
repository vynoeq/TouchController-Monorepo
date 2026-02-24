"""Utilities for parsing and managing pin files."""

def parse_pin_file(content):
    """Parse a pin file and extract hash information.

    Args:
        content: The content of the pin file as a string.

    Returns:
        A dictionary mapping URLs to their hash values.
    """
    lines = content.splitlines()
    hashes = {}
    for line in lines:
        space_index = line.find(" ")
        url = line[:space_index]
        hash = line[space_index + 1:]
        hashes[url] = hash
    return hashes
