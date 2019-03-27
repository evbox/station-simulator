import sys
import re

TABLE_HEADERS = ["Subject", "Use Case", "Supported", "Comments"]
TABLE_COLUMN_SIZE = len(TABLE_HEADERS)

def readFile(filePath):
	with open(filePath) as f:
		return list(filter(lambda line: line.strip(), [line.strip() for line in f.readlines()]))

def parseLine(subject, line):
	segments = [subject] + line.split('|')
	while len(segments) < TABLE_COLUMN_SIZE :
		segments.append("")
	return map(lambda segment: segment.strip(), segments)

def generateTableFormat(lines):
	tableFormatSizes = [0] * TABLE_COLUMN_SIZE

	subject = ""
	for line in lines:
		if re.match("[A-P][0-9]{2}", line):
			segments = parseLine(subject, line)
			for i in range(TABLE_COLUMN_SIZE):
				tableFormatSizes[i] = max(max(tableFormatSizes[i], len(segments[i])), len(TABLE_HEADERS[i]))
		else:
			subject = line

	tableFormat = ""
	for size in tableFormatSizes:
		tableFormat += "| {:<" + str(size) + "} "
	return tableFormat + "|"

def generateSeparators(tableFormat):
	separators = tableFormat.format(*([":---:"]*TABLE_COLUMN_SIZE)).replace(": ", "-:")
	while ": "  in separators: 
		separators = separators.replace(": ", "-:")
	return separators

def printTable(tableFormat, lines):

	print tableFormat.format(*TABLE_HEADERS)
	print generateSeparators(tableFormat)

	subject = ""
	for line in lines:

		if re.match("[A-P][0-9]{2}", line):
			print tableFormat.format(*parseLine(subject, line))
		else:
			subject = line

if __name__== "__main__":
	lines = readFile("coverage.txt")
	tableFormat = generateTableFormat(lines)
	printTable(tableFormat, lines)
