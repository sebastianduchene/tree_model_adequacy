

args <- commandArgs(trailingOnly=T)

ppsFileName <- args[1]
outFile <- args[2]

ppsResults <- read.table(ppsFileName, head = T, sep = '\t')
ppsResults <- ppsResults[, -c(1, ncol(ppsResults))]
pdf(file = args[2], useDingbats = F)
par(mfrow = c(3, 2))
for(i in 1:ncol(ppsResults)){
    pValue <- mean(ppsResults[1, i] > ppsResults[-1, i])
    xLabel <- paste0(colnames(ppsResults)[i], '\n', 'P-value = ', pValue)
    hist(ppsResults[-1, i], main = colnames(ppsResults)[i], col = rgb(0, 0, 1, 0.2), border = 'blue',
         xlim = range(ppsResults[, i]), xlab = xLabel)
    lines(rep(ppsResults[1, i], 2), c(0, 100), col = 'red', lwd = 3)
}
dimReduction <- svd(ppsResults)
plot(dimReduction$u[, 1], dimReduction$u[, 2], pch = 20, col = 'blue', xlab = 'SVD dimension 1',
     ylab = 'SVD dimension 2')
points(dimReduction$u[1, 1], dimReduction$u[1, 2], col = 'red', pch = 20)
dev.off()
